package com.merative.healthpass.ui.common.recyclerView

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.merative.healthpass.R
import com.merative.healthpass.common.App.Companion.context
import com.merative.healthpass.common.AppConstants
import com.merative.healthpass.extensions.*
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.models.sharedPref.getIssuerName
import com.merative.healthpass.models.sharedPref.getSchemaName
import com.merative.healthpass.utils.Utils
import com.merative.healthpass.utils.schema.DisplayFieldProcessor
import com.merative.healthpass.utils.schema.Field
import com.merative.healthpass.utils.schema.SchemaProcessor
import com.merative.watson.healthpass.verifiablecredential.extensions.SERVER_SHORT_DATE_FORMAT
import com.merative.watson.healthpass.verifiablecredential.extensions.getLocaleFormattedDate
import com.merative.watson.healthpass.verifiablecredential.extensions.parse
import com.merative.watson.healthpass.verifiablecredential.extensions.toJSONObject
import com.merative.watson.healthpass.verifiablecredential.models.VCType
import com.merative.watson.healthpass.verifiablecredential.models.veriableObject.expiryDate
import com.merative.watson.healthpass.verifiablecredential.models.veriableObject.isIDHPorGHPorVC
import com.merative.watson.healthpass.verificationengine.expiration.isExpired
import java.text.DateFormat

object ViewUtils {
    //region simple views
    fun bindSimpleHeader(
        view: View,
        section: CharSequence,
        showDivider: Boolean,
        showHeader: Boolean = true,
        removeMargin: Boolean = false,
    ) {
        view.findViewById<TextView>(R.id.srh_text).apply {
            text = if (showHeader) section else ""

            //adjust margin as needed
            val margin =
                if (removeMargin) 0
                else view.context.resources.getDimension(R.dimen.activity_vertical_margin)

            val params = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(margin.toInt(), 0, margin.toInt(), 0)

            layoutParams = params
        }
        view.findViewById<TextView>(R.id.divider_line).isVisible = showDivider
    }

    fun bindSimpleView(
        view: View,
        leftText: CharSequence, rightText: CharSequence,
        showDivider: Boolean,
        @DrawableRes drawResId: Int = AppConstants.INVALID_RES_ID,
        imageDescription: String = "",
        addPadding: Boolean = false,
    ) {

        val textViewLeft = view.findViewById<TextView>(R.id.txt_left)
        val textViewRight = view.findViewById<TextView>(R.id.txt_right)

        textViewLeft.text = leftText
        textViewRight.text = rightText.toString().replace("\\n", "\n").replace("\\r", "\n")

        view.findViewById<ImageView>(R.id.img_right).let { imgView ->
            if (drawResId == AppConstants.INVALID_RES_ID) {
                imgView.visibility = View.GONE
            } else {
                imgView.visibility = View.VISIBLE
                imgView.setImageResource(drawResId)

                if (imageDescription.isNotEmpty()) {
                    imgView.contentDescription = imageDescription

                    if (imageDescription == view.context.getString(R.string.locked)) {
                        textViewRight.text = view.context.getString(R.string.not_shared)
                        textViewRight.setTextColor(ContextCompat.getColor(context, R.color.red))
                        imgView.setColorFilter(ContextCompat.getColor(context, R.color.red), android.graphics.PorterDuff.Mode.SRC_IN)
                    }
                } else {
                    imgView.contentDescription = null
                }
            }
        }

        val divider = view.findViewById<View>(R.id.divider_line)
        divider.isVisible = showDivider

        if (addPadding) {
            view.setPadding(
                view.context.resources.getDimensionPixelSize(R.dimen.activity_vertical_margin),
                0,
                view.context.resources.getDimensionPixelSize(R.dimen.activity_vertical_margin),
                0
            )
        }
    }
    //endregion

    /**
     * This function is shared across views, be careful while doing any view's name change
     */
    @SuppressLint("SetTextI18n")
    fun adjustCredentialView(
        view: View,
        aPackage: Package,
        adjustCardColor: Boolean = true,
    ) {
        val txtName = view.findViewById<TextView>(R.id.wallet_name_textview)
        val txtIssuer = view.findViewById<TextView>(R.id.wallet_issuer_textview)
        val txtDob = view.findViewById<TextView>(R.id.dob_textView)
        val imgCredential = view.findViewById<ImageView>(R.id.wallet_credential_imageview)
        val txtExpirationDate = view.findViewById<TextView>(R.id.wallet_expiration_date_textview)
        val txtExpiredVertical = view.findViewById<TextView>(R.id.cred_item_txt_expired)
        val txtScanType = view.findViewById<TextView>(R.id.scan_type_textview)
        val txtPersonName = view.findViewById<TextView>(R.id.person_name_textView)

        val valueMapper: Map<String, Map<String, String>> = parse(loadValueMapperJson())

        //TODO Show/Hide label based on business requirement TBD
        txtScanType?.isVisible = true
        txtScanType?.text = aPackage.verifiableObject.type.displayValue

        txtName.text = aPackage.getSchemaName()

        val issuerName = aPackage.getIssuerName()
        txtIssuer?.text = if (issuerName.isEmpty()) {
            view.context.getString(R.string.issuer_unknown)
        } else {
            issuerName
        }
        val filteredList: Any?
        val fieldsList = when (aPackage.verifiableObject.type) {
            VCType.SHC -> {
                DisplayFieldProcessor(loadFieldConfigurationJson("display_shc.json", "SHC"))
                    .getDisplayFields(aPackage.verifiableObject, valueMapper)
                    .sortedBy(Field::sectionIndex)
            }
            VCType.DCC -> {
                DisplayFieldProcessor(loadFieldConfigurationJson("display_dcc.json", "DCC"))
                    .getDisplayFields(aPackage.verifiableObject, valueMapper)
                    .sortedBy(Field::sectionIndex)
            }
            else -> {
                SchemaProcessor().processSchemaAndSubject(aPackage)
            }
        }

        filteredList = fieldsList.filter { field -> field.visible }.toArrayList()
        var gName: String? = ""
        var fName: String? = ""
        var dob: String? = ""

        if (aPackage.verifiableObject.isIDHPorGHPorVC) {
            gName =
                fieldsList.find { it.path == "recipient.givenName" || it.path == "subject.name.given" }?.value.toString()
            fName =
                fieldsList.find { it.path == "recipient.familyName" || it.path == "subject.name.family" }?.value.toString()
            dob =
                fieldsList.find { it.path == "recipient.birthDate" || it.path == "subject.birthDate" }?.valueAdditionalData?.getLocaleFormattedDate(
                    SERVER_SHORT_DATE_FORMAT,
                    DateFormat.DEFAULT
                )
        } else {
            if (filteredList.isNotEmpty()) {
                gName = filteredList[0].value.toString()
                fName = filteredList[1].value.toString()
                dob = filteredList[2].valueAdditionalData?.getLocaleFormattedDate(
                    SERVER_SHORT_DATE_FORMAT,
                    DateFormat.DEFAULT
                )
            }
        }

        txtPersonName?.text = "$gName $fName"
        txtPersonName?.visibility = if (Utils.StringUtils.isAllValid(fName, gName)) View.VISIBLE else View.GONE

        if (dob.isNotNullOrEmpty()) {
            val strDob = SpannableString(dob)
            strDob.setSpan(StyleSpan(Typeface.BOLD), 0, strDob.length, 0)
            txtDob?.text = view.context.getString(R.string.dob) + " "
            txtDob?.append(strDob)
        }
        txtDob?.visibility = if ("$dob".isNotNullOrEmpty()) View.VISIBLE else View.GONE

        //TODO adjust logo based on business requirement TBD
        if (aPackage.verifiableObject.type == VCType.SHC) {
            //  TODO uncomment if logo approved
            //  imgCredential?.setImageResource(R.drawable.ic_logo_shc)
            imgCredential?.setImageFromBase64(null, null)
        } else {
            imgCredential?.setImageFromBase64(aPackage.issuerMetaData?.metadata?.logo, null)
        }

        txtExpiredVertical?.isVisible = aPackage.verifiableObject.isExpired()

        val expDateString = aPackage.verifiableObject.expiryDate
        val exp = if (aPackage.verifiableObject.isExpired()) {
            view.context.getString(R.string.result_expiredDate) + " $expDateString"
        } else {
            view.context.getString(R.string.result_expiresDate) + " $expDateString"
        }
        txtExpirationDate?.text = exp
        txtExpirationDate?.isVisible = aPackage.verifiableObject.expiryDate.isNotEmpty()

        if (aPackage.hexColor == null) {
            aPackage.processHexColor()
        }

        if (adjustCardColor) {
            val credentialView = view.findViewById<View>(R.id.layout_scanned_pass_info)
            val credentialDetailsView = view.findViewById<View>(R.id.cred_details_cardview)
            val credQrCodeCardview = view.findViewById<View>(R.id.cred_qr_code_cardview)
            setCredentialBackgroundColor(credentialView, aPackage)
            setCredentialBackgroundColor(credentialDetailsView, aPackage)
            setCredentialBackgroundColor(credQrCodeCardview, aPackage)

            if (isColorDark(fetchBackgroundColor(aPackage))) {
                txtExpirationDate?.setTextColor(ContextCompat.getColor(context, R.color.white))
                txtIssuer?.setTextColor(ContextCompat.getColor(context, R.color.white))
                txtDob?.setTextColor(ContextCompat.getColor(context, R.color.white))
                txtScanType?.setTextColor(ContextCompat.getColor(context, R.color.white))
                txtName?.setTextColor(ContextCompat.getColor(context, R.color.white))
                txtPersonName?.setTextColor(ContextCompat.getColor(context, R.color.white))
            } else {
                txtExpirationDate?.setTextColor(ContextCompat.getColor(context, R.color.black))
                txtIssuer?.setTextColor(ContextCompat.getColor(context, R.color.black))
                txtDob?.setTextColor(ContextCompat.getColor(context, R.color.black))
                txtScanType?.setTextColor(ContextCompat.getColor(context, R.color.black))
                txtName?.setTextColor(ContextCompat.getColor(context, R.color.black))
                txtPersonName?.setTextColor(ContextCompat.getColor(context, R.color.black))
            }
        }
    }

    private fun setCredentialBackgroundColor(view: View?, aPackage: Package) {
        val cardView: CardView = when {
            view is CardView -> view
            view?.parent is CardView -> view.parent as CardView
            else -> return
        }
        val colors = intArrayOf(
            Color.parseColor(AppConstants.COLOR_CREDENTIALS),
            Color.parseColor(
                aPackage.hexColor
                    .orValue(AppConstants.COLOR_CREDENTIALS)
            )
        )
        val gd = GradientDrawable(GradientDrawable.Orientation.TR_BL, colors)
        gd.setColor(fetchBackgroundColor(aPackage))
        gd.cornerRadius = view.context.resources.getDimension(R.dimen.card_corner)

        cardView.background = gd
    }

    private fun fetchBackgroundColor(aPackage: Package): Int {
        return if (aPackage.verifiableObject.isIDHPorGHPorVC) {
            Color.parseColor(aPackage.hexColor)
        } else {
            val resId = when (aPackage.verifiableObject.type) {
                VCType.SHC -> R.color.credentialColorShc
                VCType.DCC -> R.color.credentialColorDcc
                VCType.CONTACT -> R.color.contactColor
                else -> R.color.credentialColor
            }
            ContextCompat.getColor(context, resId)
        }
    }

    fun isColorDark(color: Int): Boolean {
        val darkness =
            1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness >= 0.50
    }

    fun loadValueMapperJson() =
        context.loadAssetsFileAsString("value-mapper.json")
            .toJSONObject()
            ?.toString()
            .orValue("{}")

    fun loadFieldConfigurationJson(assetName: String, type: String) =
        context.loadAssetsFileAsString(assetName)
            .toJSONObject()
            ?.getJSONObject("configuration")
            ?.getJSONObject(type)
            ?.getJSONArray("details")
            ?.toString()
            .orValue("{}")
}