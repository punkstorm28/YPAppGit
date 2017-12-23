LOCAL_PATH := $(call my-dir)
TOP_LOCAL_PATH := $(LOCAL_PATH)

MUPDF_ROOT := $(TOP_LOCAL_PATH)/mupdf

ifdef NDK_PROFILER
include android-ndk-profiler.mk
endif

include $(TOP_LOCAL_PATH)/Core.mk

include $(TOP_LOCAL_PATH)/ThirdParty.mk

include $(CLEAR_VARS)
$(warning apples $(MUPDF_ROOT))

LOCAL_C_INCLUDES := \
	jni/andprof \
	$(MUPDF_ROOT)/include \
	$(MUPDF_ROOT)/source/fitz \
	$(MUPDF_ROOT)/source/pdf


LOCAL_MODULE    := Mupdf


LOCAL_STATIC_LIBRARIES := mupdfcore mupdfthirdparty
LOCAL_SRC_FILES := $(TOP_LOCAL_PATH)/amupdf.c\
$(TOP_LOCAL_PATH)/bytebufferbitmapbridge.c

ifdef NDK_PROFILER
LOCAL_CFLAGS += -pg -DNDK_PROFILER
LOCAL_STATIC_LIBRARIES += andprof
endif
ifdef SUPPORT_GPROOF
LOCAL_CFLAGS += -DSUPPORT_GPROOF
endif

LOCAL_LDLIBS    := -lm -llog -ljnigraphics
ifdef SSL_BUILD
LOCAL_LDLIBS	+= -L$(MUPDF_ROOT)/thirdparty/openssl/android -lcrypto -lssl
endif

include $(BUILD_SHARED_LIBRARY)
