package com.example.rtse.domain

sealed class PermissionCheckState {

    object InProgress : PermissionCheckState()

    object Complete : PermissionCheckState()
}