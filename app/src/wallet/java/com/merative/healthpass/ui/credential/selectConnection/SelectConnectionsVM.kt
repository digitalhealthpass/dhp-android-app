package com.merative.healthpass.ui.credential.selectConnection

import com.merative.healthpass.models.sharedPref.ContactPackage
import com.merative.healthpass.ui.common.BaseViewModel
import com.merative.healthpass.utils.pref.ContactDB
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject
import javax.inject.Inject

class SelectConnectionsVM @Inject constructor(
    private val contactDB: ContactDB
) : BaseViewModel() {

    var selectedConnections = ArrayList<ContactPackage>()
    val isListEmpty: BehaviorSubject<Boolean> =
        BehaviorSubject.createDefault(selectedConnections.isEmpty())

    /**
     * @return a list of available Connections
     */
    fun loadAllConnections(): Single<ArrayList<ContactPackage>> {
        return contactDB.loadAll().map { it.dataList }
    }

    fun removeAndAddContacts(cPackage: ContactPackage) {
        if (selectedConnections.contains(cPackage)) {
            selectedConnections.remove(cPackage)
        }
        selectedConnections.add(cPackage)

        isListEmpty.onNext(selectedConnections.isEmpty())
    }
}