# base recipe : meta/recipes-support/ca-certificates/ca-certificates_20190110.bb
# base branch : warrior
# base commit : 15f2cefac48f0e1d8f0e921b858df2355da5bbcd

SUMMARY = "Common CA certificates"
DESCRIPTION = "This package includes PEM files of CA certificates to allow \
SSL-based applications to check for the authenticity of SSL connections. \
This derived from Debian's CA Certificates."
HOMEPAGE = "http://packages.debian.org/sid/ca-certificates"
SECTION = "misc"
LICENSE = "GPL-2.0+ & MPL-2.0"
LIC_FILES_CHKSUM = "file://debian/copyright;md5=aeb420429b1659507e0a5a1b123e8308"

inherit debian-package
require recipes-debian/sources/ca-certificates.inc
FILESPATH_append = ":${COREBASE}/meta/recipes-support/ca-certificates/ca-certificates"
DEBIAN_PATCH_TYPE = "nopatch"

# This is needed to ensure we can run the postinst at image creation time
DEPENDS = ""
DEPENDS_class-native = "openssl-native"
DEPENDS_class-nativesdk = "openssl-native"
# Need rehash from openssl and run-parts from debianutils
PACKAGE_WRITE_DEPS += "openssl-native debianutils-native"

SRC_URI += " \
           file://0002-update-ca-certificates-use-SYSROOT.patch \
           file://0001-update-ca-certificates-don-t-use-Debianisms-in-run-p.patch \
           file://update-ca-certificates-support-Toybox.patch \
           file://default-sysroot.patch \
           file://sbindir.patch \
           file://0003-update-ca-certificates-use-relative-symlinks-from-ET.patch \
           "

inherit allarch

EXTRA_OEMAKE = "\
    'CERTSDIR=${datadir}/ca-certificates' \
    'SBINDIR=${sbindir}' \
"

do_compile_prepend() {
    oe_runmake clean
}

do_install () {
    install -d ${D}${datadir}/ca-certificates \
               ${D}${sysconfdir}/ssl/certs \
               ${D}${sysconfdir}/ca-certificates/update.d
    oe_runmake 'DESTDIR=${D}' install

    install -d ${D}${mandir}/man8
    install -m 0644 sbin/update-ca-certificates.8 ${D}${mandir}/man8/

    install -d ${D}${sysconfdir}
    {
        echo "# Lines starting with # will be ignored"
        echo "# Lines starting with ! will remove certificate on next update"
        echo "#"
        find ${D}${datadir}/ca-certificates -type f -name '*.crt' | \
            sed 's,^${D}${datadir}/ca-certificates/,,'
    } >${D}${sysconfdir}/ca-certificates.conf
}

do_install_append_class-target () {
    sed -i -e 's,/etc/,${sysconfdir}/,' \
           -e 's,/usr/share/,${datadir}/,' \
           -e 's,/usr/local,${prefix}/local,' \
        ${D}${sbindir}/update-ca-certificates \
        ${D}${mandir}/man8/update-ca-certificates.8
}

pkg_postinst_${PN}_class-target () {
    SYSROOT="$D" $D${sbindir}/update-ca-certificates
}

CONFFILES_${PN} += "${sysconfdir}/ca-certificates.conf"

# Rather than make a postinst script that works for both target and nativesdk,
# we just run update-ca-certificate from do_install() for nativesdk.
CONFFILES_${PN}_append_class-nativesdk = " ${sysconfdir}/ssl/certs/ca-certificates.crt"
do_install_append_class-nativesdk () {
    SYSROOT="${D}${SDKPATHNATIVE}" ${D}${sbindir}/update-ca-certificates
}

do_install_append_class-native () {
    SYSROOT="${D}${base_prefix}" ${D}${sbindir}/update-ca-certificates
}

RDEPENDS_${PN} += "openssl"

BBCLASSEXTEND = "native nativesdk"
