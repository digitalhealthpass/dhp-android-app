package com.merative.healthpass.network.repository

import com.merative.healthpass.utils.asyncToUiSingle
import com.merative.watson.healthpass.verifiablecredential.models.VCType
import com.merative.watson.healthpass.verifiablecredential.models.cose.cwt
import com.merative.watson.healthpass.verifiablecredential.models.veriableObject.VerifiableObject
import com.merative.watson.healthpass.verifiablecredential.models.veriableObject.isIDHPorGHPorVC
import io.reactivex.rxjava3.core.Single
import java.util.*
import javax.inject.Inject

class IssuerMetaDataRepo @Inject constructor(
    private val schemaRepo: SchemaRepo,
    private val signatureRepo: IssuerRepo,
) {
    /**
     * Get the issuer name for this verifiable object.
     * this can be from DB/File or an API request
     */
    fun fetchIssuerName(verifiableObject: VerifiableObject): Single<Optional<String>> {
        return when {
            verifiableObject.isIDHPorGHPorVC -> fetchIssuerMetaData(verifiableObject)
            verifiableObject.type == VCType.SHC -> fetchSmartHealthIssuer(verifiableObject)
            verifiableObject.type == VCType.DCC -> fetchEUDCCIssuer(verifiableObject)
            else -> Single.just(Optional.ofNullable(null))
        }
    }

    private fun fetchIssuerMetaData(verifiableObject: VerifiableObject): Single<Optional<String>> {
        if (verifiableObject.credential == null)
            return Single.just(Optional.ofNullable(null))

        return schemaRepo.fetchMetaData(verifiableObject.credential!!)
            .asyncToUiSingle()
            .map {
                Optional.ofNullable(it.body()?.payload?.metadata?.name)
            }
    }

    private fun fetchSmartHealthIssuer(verifiableObject: VerifiableObject): Single<Optional<String>> {
        if (verifiableObject.jws?.payload?.iss == null)
            return Single.just(Optional.ofNullable(null))

        return signatureRepo.getIssuerFromDb(verifiableObject)
            .asyncToUiSingle()
            .map {
                Optional.ofNullable(it.issuerName)
            }
    }

    private fun fetchEUDCCIssuer(verifiableObject: VerifiableObject): Single<Optional<String>> {
        return Single.fromCallable {
            //region countries map
            val EUDCCMap = arrayOf(
                mapOf("name" to "Afghanistan", "code" to "AF"),
                mapOf("name" to "Åland Islands", "code" to "AX"),
                mapOf("name" to "Albania", "code" to "AL"),
                mapOf("name" to "Algeria", "code" to "DZ"),
                mapOf("name" to "American Samoa", "code" to "AS"),
                mapOf("name" to "AndorrA", "code" to "AD"),
                mapOf("name" to "Angola", "code" to "AO"),
                mapOf("name" to "Anguilla", "code" to "AI"),
                mapOf("name" to "Antarctica", "code" to "AQ"),
                mapOf("name" to "Antigua and Barbuda", "code" to "AG"),
                mapOf("name" to "Argentina", "code" to "AR"),
                mapOf("name" to "Armenia", "code" to "AM"),
                mapOf("name" to "Aruba", "code" to "AW"),
                mapOf("name" to "Australia", "code" to "AU"),
                mapOf("name" to "Austria", "code" to "AT"),
                mapOf("name" to "Azerbaijan", "code" to "AZ"),
                mapOf("name" to "Bahamas", "code" to "BS"),
                mapOf("name" to "Bahrain", "code" to "BH"),
                mapOf("name" to "Bangladesh", "code" to "BD"),
                mapOf("name" to "Barbados", "code" to "BB"),
                mapOf("name" to "Belarus", "code" to "BY"),
                mapOf("name" to "Belgium", "code" to "BE"),
                mapOf("name" to "Belize", "code" to "BZ"),
                mapOf("name" to "Benin", "code" to "BJ"),
                mapOf("name" to "Bermuda", "code" to "BM"),
                mapOf("name" to "Bhutan", "code" to "BT"),
                mapOf("name" to "Bolivia", "code" to "BO"),
                mapOf("name" to "Bosnia and Herzegovina", "code" to "BA"),
                mapOf("name" to "Botswana", "code" to "BW"),
                mapOf("name" to "Bouvet Island", "code" to "BV"),
                mapOf("name" to "Brazil", "code" to "BR"),
                mapOf("name" to "British Indian Ocean Territory", "code" to "IO"),
                mapOf("name" to "Brunei Darussalam", "code" to "BN"),
                mapOf("name" to "Bulgaria", "code" to "BG"),
                mapOf("name" to "Burkina Faso", "code" to "BF"),
                mapOf("name" to "Burundi", "code" to "BI"),
                mapOf("name" to "Cambodia", "code" to "KH"),
                mapOf("name" to "Cameroon", "code" to "CM"),
                mapOf("name" to "Canada", "code" to "CA"),
                mapOf("name" to "Cape Verde", "code" to "CV"),
                mapOf("name" to "Cayman Islands", "code" to "KY"),
                mapOf("name" to "Central African Republic", "code" to "CF"),
                mapOf("name" to "Chad", "code" to "TD"),
                mapOf("name" to "Chile", "code" to "CL"),
                mapOf("name" to "China", "code" to "CN"),
                mapOf("name" to "Christmas Island", "code" to "CX"),
                mapOf("name" to "Cocos (Keeling) Islands", "code" to "CC"),
                mapOf("name" to "Colombia", "code" to "CO"),
                mapOf("name" to "Comoros", "code" to "KM"),
                mapOf("name" to "Congo", "code" to "CG"),
                mapOf("name" to "Congo, The Democratic Republic of the", "code" to "CD"),
                mapOf("name" to "Cook Islands", "code" to "CK"),
                mapOf("name" to "Costa Rica", "code" to "CR"),
                mapOf("name" to "Cote D\"Ivoire", "code" to "CI"),
                mapOf("name" to "Croatia", "code" to "HR"),
                mapOf("name" to "Cuba", "code" to "CU"),
                mapOf("name" to "Cyprus", "code" to "CY"),
                mapOf("name" to "Czech Republic", "code" to "CZ"),
                mapOf("name" to "Denmark", "code" to "DK"),
                mapOf("name" to "Djibouti", "code" to "DJ"),
                mapOf("name" to "Dominica", "code" to "DM"),
                mapOf("name" to "Dominican Republic", "code" to "DO"),
                mapOf("name" to "Ecuador", "code" to "EC"),
                mapOf("name" to "Egypt", "code" to "EG"),
                mapOf("name" to "El Salvador", "code" to "SV"),
                mapOf("name" to "Equatorial Guinea", "code" to "GQ"),
                mapOf("name" to "Eritrea", "code" to "ER"),
                mapOf("name" to "Estonia", "code" to "EE"),
                mapOf("name" to "Ethiopia", "code" to "ET"),
                mapOf("name" to "Falkland Islands (Malvinas)", "code" to "FK"),
                mapOf("name" to "Faroe Islands", "code" to "FO"),
                mapOf("name" to "Fiji", "code" to "FJ"),
                mapOf("name" to "Finland", "code" to "FI"),
                mapOf("name" to "France", "code" to "FR"),
                mapOf("name" to "French Guiana", "code" to "GF"),
                mapOf("name" to "French Polynesia", "code" to "PF"),
                mapOf("name" to "French Southern Territories", "code" to "TF"),
                mapOf("name" to "Gabon", "code" to "GA"),
                mapOf("name" to "Gambia", "code" to "GM"),
                mapOf("name" to "Georgia", "code" to "GE"),
                mapOf("name" to "Germany", "code" to "DE"),
                mapOf("name" to "Ghana", "code" to "GH"),
                mapOf("name" to "Gibraltar", "code" to "GI"),
                mapOf("name" to "Greece", "code" to "GR"),
                mapOf("name" to "Greenland", "code" to "GL"),
                mapOf("name" to "Grenada", "code" to "GD"),
                mapOf("name" to "Guadeloupe", "code" to "GP"),
                mapOf("name" to "Guam", "code" to "GU"),
                mapOf("name" to "Guatemala", "code" to "GT"),
                mapOf("name" to "Guernsey", "code" to "GG"),
                mapOf("name" to "Guinea", "code" to "GN"),
                mapOf("name" to "Guinea-Bissau", "code" to "GW"),
                mapOf("name" to "Guyana", "code" to "GY"),
                mapOf("name" to "Haiti", "code" to "HT"),
                mapOf("name" to "Heard Island and Mcdonald Islands", "code" to "HM"),
                mapOf("name" to "Holy See (Vatican City State)", "code" to "VA"),
                mapOf("name" to "Honduras", "code" to "HN"),
                mapOf("name" to "Hong Kong", "code" to "HK"),
                mapOf("name" to "Hungary", "code" to "HU"),
                mapOf("name" to "Iceland", "code" to "IS"),
                mapOf("name" to "India", "code" to "IN"),
                mapOf("name" to "Indonesia", "code" to "ID"),
                mapOf("name" to "Iran, Islamic Republic Of", "code" to "IR"),
                mapOf("name" to "Iraq", "code" to "IQ"),
                mapOf("name" to "Ireland", "code" to "IE"),
                mapOf("name" to "Isle of Man", "code" to "IM"),
                mapOf("name" to "Israel", "code" to "IL"),
                mapOf("name" to "Italy", "code" to "IT"),
                mapOf("name" to "Jamaica", "code" to "JM"),
                mapOf("name" to "Japan", "code" to "JP"),
                mapOf("name" to "Jersey", "code" to "JE"),
                mapOf("name" to "Jordan", "code" to "JO"),
                mapOf("name" to "Kazakhstan", "code" to "KZ"),
                mapOf("name" to "Kenya", "code" to "KE"),
                mapOf("name" to "Kiribati", "code" to "KI"),
                mapOf("name" to "Korea, Democratic People\"S Republic of", "code" to "KP"),
                mapOf("name" to "Korea, Republic of", "code" to "KR"),
                mapOf("name" to "Kuwait", "code" to "KW"),
                mapOf("name" to "Kyrgyzstan", "code" to "KG"),
                mapOf("name" to "Lao People\"S Democratic Republic", "code" to "LA"),
                mapOf("name" to "Latvia", "code" to "LV"),
                mapOf("name" to "Lebanon", "code" to "LB"),
                mapOf("name" to "Lesotho", "code" to "LS"),
                mapOf("name" to "Liberia", "code" to "LR"),
                mapOf("name" to "Libyan Arab Jamahiriya", "code" to "LY"),
                mapOf("name" to "Liechtenstein", "code" to "LI"),
                mapOf("name" to "Lithuania", "code" to "LT"),
                mapOf("name" to "Luxembourg", "code" to "LU"),
                mapOf("name" to "Macao", "code" to "MO"),
                mapOf("name" to "Macedonia, The Former Yugoslav Republic of", "code" to "MK"),
                mapOf("name" to "Madagascar", "code" to "MG"),
                mapOf("name" to "Malawi", "code" to "MW"),
                mapOf("name" to "Malaysia", "code" to "MY"),
                mapOf("name" to "Maldives", "code" to "MV"),
                mapOf("name" to "Mali", "code" to "ML"),
                mapOf("name" to "Malta", "code" to "MT"),
                mapOf("name" to "Marshall Islands", "code" to "MH"),
                mapOf("name" to "Martinique", "code" to "MQ"),
                mapOf("name" to "Mauritania", "code" to "MR"),
                mapOf("name" to "Mauritius", "code" to "MU"),
                mapOf("name" to "Mayotte", "code" to "YT"),
                mapOf("name" to "Mexico", "code" to "MX"),
                mapOf("name" to "Micronesia, Federated States of", "code" to "FM"),
                mapOf("name" to "Moldova, Republic of", "code" to "MD"),
                mapOf("name" to "Monaco", "code" to "MC"),
                mapOf("name" to "Mongolia", "code" to "MN"),
                mapOf("name" to "Montserrat", "code" to "MS"),
                mapOf("name" to "Morocco", "code" to "MA"),
                mapOf("name" to "Mozambique", "code" to "MZ"),
                mapOf("name" to "Myanmar", "code" to "MM"),
                mapOf("name" to "Namibia", "code" to "NA"),
                mapOf("name" to "Nauru", "code" to "NR"),
                mapOf("name" to "Nepal", "code" to "NP"),
                mapOf("name" to "Netherlands", "code" to "NL"),
                mapOf("name" to "Netherlands Antilles", "code" to "AN"),
                mapOf("name" to "New Caledonia", "code" to "NC"),
                mapOf("name" to "New Zealand", "code" to "NZ"),
                mapOf("name" to "Nicaragua", "code" to "NI"),
                mapOf("name" to "Niger", "code" to "NE"),
                mapOf("name" to "Nigeria", "code" to "NG"),
                mapOf("name" to "Niue", "code" to "NU"),
                mapOf("name" to "Norfolk Island", "code" to "NF"),
                mapOf("name" to "Northern Mariana Islands", "code" to "MP"),
                mapOf("name" to "Norway", "code" to "NO"),
                mapOf("name" to "Oman", "code" to "OM"),
                mapOf("name" to "Pakistan", "code" to "PK"),
                mapOf("name" to "Palau", "code" to "PW"),
                mapOf("name" to "Palestinian Territory, Occupied", "code" to "PS"),
                mapOf("name" to "Panama", "code" to "PA"),
                mapOf("name" to "Papua New Guinea", "code" to "PG"),
                mapOf("name" to "Paraguay", "code" to "PY"),
                mapOf("name" to "Peru", "code" to "PE"),
                mapOf("name" to "Philippines", "code" to "PH"),
                mapOf("name" to "Pitcairn", "code" to "PN"),
                mapOf("name" to "Poland", "code" to "PL"),
                mapOf("name" to "Portugal", "code" to "PT"),
                mapOf("name" to "Puerto Rico", "code" to "PR"),
                mapOf("name" to "Qatar", "code" to "QA"),
                mapOf("name" to "Reunion", "code" to "RE"),
                mapOf("name" to "Romania", "code" to "RO"),
                mapOf("name" to "Russian Federation", "code" to "RU"),
                mapOf("name" to "RWANDA", "code" to "RW"),
                mapOf("name" to "Saint Helena", "code" to "SH"),
                mapOf("name" to "Saint Kitts and Nevis", "code" to "KN"),
                mapOf("name" to "Saint Lucia", "code" to "LC"),
                mapOf("name" to "Saint Pierre and Miquelon", "code" to "PM"),
                mapOf("name" to "Saint Vincent and the Grenadines", "code" to "VC"),
                mapOf("name" to "Samoa", "code" to "WS"),
                mapOf("name" to "San Marino", "code" to "SM"),
                mapOf("name" to "Sao Tome and Principe", "code" to "ST"),
                mapOf("name" to "Saudi Arabia", "code" to "SA"),
                mapOf("name" to "Senegal", "code" to "SN"),
                mapOf("name" to "Serbia and Montenegro", "code" to "CS"),
                mapOf("name" to "Seychelles", "code" to "SC"),
                mapOf("name" to "Sierra Leone", "code" to "SL"),
                mapOf("name" to "Singapore", "code" to "SG"),
                mapOf("name" to "Slovakia", "code" to "SK"),
                mapOf("name" to "Slovenia", "code" to "SI"),
                mapOf("name" to "Solomon Islands", "code" to "SB"),
                mapOf("name" to "Somalia", "code" to "SO"),
                mapOf("name" to "South Africa", "code" to "ZA"),
                mapOf("name" to "South Georgia and the South Sandwich Islands", "code" to "GS"),
                mapOf("name" to "Spain", "code" to "ES"),
                mapOf("name" to "Sri Lanka", "code" to "LK"),
                mapOf("name" to "Sudan", "code" to "SD"),
                mapOf("name" to "Suriname", "code" to "SR"),
                mapOf("name" to "Svalbard and Jan Mayen", "code" to "SJ"),
                mapOf("name" to "Swaziland", "code" to "SZ"),
                mapOf("name" to "Sweden", "code" to "SE"),
                mapOf("name" to "Switzerland", "code" to "CH"),
                mapOf("name" to "Syrian Arab Republic", "code" to "SY"),
                mapOf("name" to "Taiwan, Province of China", "code" to "TW"),
                mapOf("name" to "Tajikistan", "code" to "TJ"),
                mapOf("name" to "Tanzania, United Republic of", "code" to "TZ"),
                mapOf("name" to "Thailand", "code" to "TH"),
                mapOf("name" to "Timor-Leste", "code" to "TL"),
                mapOf("name" to "Togo", "code" to "TG"),
                mapOf("name" to "Tokelau", "code" to "TK"),
                mapOf("name" to "Tonga", "code" to "TO"),
                mapOf("name" to "Trinidad and Tobago", "code" to "TT"),
                mapOf("name" to "Tunisia", "code" to "TN"),
                mapOf("name" to "Turkey", "code" to "TR"),
                mapOf("name" to "Turkmenistan", "code" to "TM"),
                mapOf("name" to "Turks and Caicos Islands", "code" to "TC"),
                mapOf("name" to "Tuvalu", "code" to "TV"),
                mapOf("name" to "Uganda", "code" to "UG"),
                mapOf("name" to "Ukraine", "code" to "UA"),
                mapOf("name" to "United Arab Emirates", "code" to "AE"),
                mapOf("name" to "United Kingdom", "code" to "GB"),
                mapOf("name" to "United States", "code" to "US"),
                mapOf("name" to "United States Minor Outlying Islands", "code" to "UM"),
                mapOf("name" to "Uruguay", "code" to "UY"),
                mapOf("name" to "Uzbekistan", "code" to "UZ"),
                mapOf("name" to "Vanuatu", "code" to "VU"),
                mapOf("name" to "Venezuela", "code" to "VE"),
                mapOf("name" to "Viet Nam", "code" to "VN"),
                mapOf("name" to "Virgin Islands, British", "code" to "VG"),
                mapOf("name" to "Virgin Islands, U.S.", "code" to "VI"),
                mapOf("name" to "Wallis and Futuna", "code" to "WF"),
                mapOf("name" to "Western Sahara", "code" to "EH"),
                mapOf("name" to "Yemen", "code" to "YE"),
                mapOf("name" to "Zambia", "code" to "ZM"),
                mapOf("name" to "Zimbabwe", "code" to "ZW")
            )
            //endregion

            val iss = verifiableObject.cose?.cwt?.iss
            val x: Map<String, String>? = EUDCCMap.firstOrNull { it["code"] == iss }

            if (x.isNullOrEmpty()) {
                Optional.ofNullable(iss)
            } else
                Optional.ofNullable(
                    x.get("name")
                )
        }
    }

    companion object {
        private const val VCI_PATH = "vci-issuers.json"
    }
}