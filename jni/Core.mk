LOCAL_PATH := $(call my-dir)

ifdef SUPPORT_GPROOF
include $(CLEAR_VARS)
LOCAL_MODULE    := gsso
LOCAL_SRC_FILES := libgs.so
include $(PREBUILT_SHARED_LIBRARY)
endif

include $(CLEAR_VARS)

MY_ROOT := $(LOCAL_PATH)/mupdf/


ifeq ($(TARGET_ARCH),arm)
LOCAL_CFLAGS += -DARCH_ARM -DARCH_THUMB -DARCH_ARM_CAN_LOAD_UNALIGNED
ifdef NDK_PROFILER
LOCAL_CFLAGS += -pg -DNDK_PROFILER
endif
endif
ifdef SUPPORT_GPROOF
LOCAL_CFLAGS += -DSUPPORT_GPROOF
endif
LOCAL_CFLAGS += -DAA_BITS=8
ifdef MEMENTO
LOCAL_CFLAGS += -DMEMENTO -DMEMENTO_LEAKONLY
endif
ifdef SSL_BUILD
LOCAL_CFLAGS += -DHAVE_OPENSSL
endif
$(warning at Core.mk Mupdf source root is $(MY_ROOT))
$(warning print complete)
LOCAL_C_INCLUDES := \
	$(MY_ROOT)/thirdparty/jbig2dec \
	$(MY_ROOT)/thirdparty/openjpeg/libopenjpeg \
	$(MY_ROOT)/thirdparty/jpeg \
	$(MY_ROOT)/thirdparty/mujs \
	$(MY_ROOT)/thirdparty/zlib \
	$(MY_ROOT)/thirdparty/freetype/include \
	$(MY_ROOT)/source/fitz \
	$(MY_ROOT)/source/pdf \
	$(MY_ROOT)/source/xps \
	$(MY_ROOT)/source/cbz \
	$(MY_ROOT)/source/img \
	$(MY_ROOT)/source/tiff \
	$(MY_ROOT)/scripts/freetype \
	$(MY_ROOT)/scripts/jpeg \
	$(MY_ROOT)/scripts/openjpeg \
	$(MY_ROOT)/generated \
	$(MY_ROOT)/resources\
	$(MY_ROOT)/include

$(warning INCLUSION DONE)

ifdef V8_BUILD
LOCAL_C_INCLUDES += $(MY_ROOT)/thirdparty/$(V8)/include
endif
ifdef SSL_BUILD
LOCAL_C_INCLUDES += $(MY_ROOT)/thirdparty/openssl/include
endif

LOCAL_MODULE    := mupdfcore
LOCAL_SRC_FILES := \
	$(wildcard /source/fitz/*.c) \
	$(wildcard /source/pdf/*.c) \
	$(wildcard /source/xps/*.c) \
	$(wildcard /source/cbz/*.c) \
	$(wildcard /source/gprf/*.c) \
	$(wildcard /source/html/*.c)

LOCAL_SRC_FILES += /source/pdf/js/pdf-js.c

ifdef SUPPORT_GPROOF
LOCAL_SHARED_LIBRARIES := gsso
endif
LOCAL_LDLIBS    := -lm -llog -ljnigraphics

LOCAL_SRC_FILES := $(addprefix $(MY_ROOT), $(LOCAL_SRC_FILES))

include $(BUILD_STATIC_LIBRARY)
$(warning STATIC BUILD DONE_Core)
