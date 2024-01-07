package com.example.rtse

import com.example.rtse.domain.PermissionCheckState

data class MainActivityState(
    val permissionCheckStatus : PermissionCheckState = PermissionCheckState.InProgress
)