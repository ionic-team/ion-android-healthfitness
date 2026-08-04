package io.ionic.libs.ionhealthfitnesslib.data

enum class HealthFitnessError(val code: Int, val message: String) {
    //Health Connect SDK
    PERMISSIONS_DENIED_ERROR(300, "Some required permissions have not been granted. Ensure all necessary permissions are enabled for this action to be completed."),
    HEALTH_CONNECT_NOT_AVAILABLE_ERROR(301, "Health connect isn't available for this version of Android."),
    HEALTH_CONNECT_NOT_INSTALLED_OR_UPDATED_ERROR(302, "Health Connect isn't installed on the device, or needs to be updated."),
    HEALTH_CONNECT_NOT_INSTALLED_ERROR(303, "Health Connect isn't installed on the device."),
    OPEN_HEALTH_CONNECT_ERROR(304, "There was an error opening the Health Connect app."),
    DISABLE_HEALTH_CONNECT_ERROR(305, "There was an error disabling Health Connect."),

    // Plugin Shared errors
    REQUEST_PERMISSIONS_GENERAL_ERROR(114, "There was an error requesting the variable permissions."),
    PARSING_PARAMETERS_ERROR(113, "There was an error parsing the parameters."),
    VARIABLE_NOT_AVAILABLE_ERROR(100, "Variable not available."),
    VARIABLE_NOT_AUTHORIZED_ERROR(101, "Variable not authorized."),
    OPERATION_NOT_ALLOWED(102, "Operation not allowed."),
    READ_DATA_ERROR(103, "Error while reading data."),
    READ_DATA_NO_PERMISSIONS(117, "Read permission for this variable hasn't been granted."),
    WRITE_DATA_ERROR(104, "Error while writing data."),
    WRITE_DATA_NOT_PROFILE_ERROR(115, "Variable either doesn't exist or isn't a profile variable."),
    WRITE_DATA_NO_PERMISSIONS(116, "Write permission for this variable hasn't been given."),
    BACKGROUND_JOB_ALREADY_EXISTS_ERROR(105, "The background job you are trying to set already exists."),
    BACKGROUND_JOB_GENERIC_ERROR(106, "The background job couldn't be created."),
    BACKGROUND_JOB_DOES_NOT_EXIST_ERROR(107, "The background job couldn't be found."),
    UNSUBSCRIBE_ERROR(108, "The background job couldn't be deleted."),
    LIST_BACKGROUND_JOBS_GENERIC_ERROR(110, "The list of background jobs couldn't be fetched."),
    DELETE_BACKGROUND_JOB_GENERIC_ERROR(111, "The background job couldn't be deleted."),
    UPDATE_BACKGROUND_JOB_GENERIC_ERROR(112, "The background job couldn't be updated."),
    PERMISSIONS_DENIED_READ_ERROR(113, "There is no read permission for this variable."),

    // Plugin Android specific errors
    WRITE_VALUE_OUT_OF_RANGE_ERROR(109, "Value provided is out of range for this variable."),

    // Overall Android specific
    GOOGLE_SERVICES_RESOLVABLE_ERROR (200, "Google Play services aren't available on your device."),
    GOOGLE_SERVICES_ERROR (201, "Google Play Services error."),
    DISABLE_GOOGLE_FIT_ERROR(202, "There was an error disabling Google Fit."),

    // Background job permissions
    BACKGROUND_JOB_PERMISSIONS_DENIED_ERROR(400, "Permissions for background job were denied."),
    BACKGROUND_JOB_EXACT_ALARM_PERMISSION_DENIED_ERROR(402, "Permission to schedule exact alarms was denied. Please grant the permission to schedule exact alarms in order to use this feature."),
    BACKGROUND_JOB_READ_DATA_PERMISSION_DENIED(403, "Permission to read health data in the background was denied. Please grant the permission to read health data in the background in order to use this feature."),

    //PRIVACY_POLICY_ERROR
    PRIVACY_POLICY_NOT_DEFINED_ERROR(401, "The Privacy Policy URL is missing. Define the Privacy Policy URL before proceeding.")
}