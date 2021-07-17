package com.jodhpurtechies.audioplayer.utils

import android.Manifest
import android.app.Activity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener


object AndroidPermissions {

    fun askReadWritePermissions(
        activity: Activity,
        onPermissionsGranted: () -> Unit,
        onPermissionError: (msg: String) -> Unit
    ) {
        Dexter.withContext(activity)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(permissionReport: MultiplePermissionsReport?) {
                    permissionReport?.apply {
                        if (areAllPermissionsGranted()) {
                            onPermissionsGranted()
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    listPermissionRequest: MutableList<PermissionRequest>?,
                    permissionToken: PermissionToken?
                ) {
                    permissionToken?.continuePermissionRequest()
                }
            })
            .withErrorListener {
                onPermissionError(it.name)
            }
            .check()
    }

}