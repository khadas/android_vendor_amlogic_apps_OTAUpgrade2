LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_STATIC_JAVA_LIBRARIES := libota
LOCAL_SDK_VERSION := current
LOCAL_SRC_FILES := $(call all-java-files-under, src)

ifneq ($(PLATFORM_SDK_VERSION),23)
    LOCAL_SRC_FILES += $(TOP)/src/android/os/IUpdateEngineCallback.aidl
endif
LOCAL_PACKAGE_NAME := OTAUpgrade
LOCAL_CERTIFICATE := platform
LOCAL_DEX_PREOPT := false
LOCAL_PROGUARD_ENABLED := full
LOCAL_PROGUARD_FLAGS := -include $(LOCAL_PATH)/proguard.flags

include $(BUILD_PACKAGE)
##############################################

include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := libota:libs/libotaupdate.jar

include $(BUILD_MULTI_PREBUILT)



# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
