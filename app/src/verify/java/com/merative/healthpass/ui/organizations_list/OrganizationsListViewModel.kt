package com.merative.healthpass.ui.organizations_list

import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.ui.common.BaseViewModel
import com.merative.healthpass.utils.pref.PackageDB
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class OrganizationsListViewModel @Inject constructor(
    val packageDB: PackageDB,
) : BaseViewModel() {

    fun loadData(): Single<Pair<List<Package>, List<Package>>> {
        return packageDB.loadAll()
            .map {
                val availableList = ArrayList<Package>()
                val expiredList = ArrayList<Package>()
                it.dataList.forEach { cp ->
                    if (cp.verifiableObject.credential!!.isExpired()) {
                        if (cp.isSelected) {
                            cp.isSelected = false
                        }
                        expiredList.add(cp)
                    } else {
                        availableList.add(cp)
                    }
                }

                availableList to expiredList
            }
    }
}