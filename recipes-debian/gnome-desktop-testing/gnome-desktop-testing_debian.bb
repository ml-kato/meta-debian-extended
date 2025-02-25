# base recipe: meta/recipes-support/gnome-desktop-testing/gnome-desktop-testing_2018.1.bb
# base branch: warrior
# base commit: bd57916ae56669f563c7aa816ac520dc37cfc78d

SUMMARY = "Test runner for GNOME-style installed tests"
HOMEPAGE = "https://wiki.gnome.org/GnomeGoals/InstalledTests"
LICENSE = "LGPLv2+"

inherit debian-package
require recipes-debian/sources/gnome-desktop-testing.inc

LIC_FILES_CHKSUM = "file://COPYING;md5=3bf50002aefd002f49e7bb854063f7e7 \
                    file://src/gnome-desktop-testing-runner.c;beginline=1;endline=20;md5=7ef3ad9da2ffcf7707dc11151fe007f4"

DEPENDS = "glib-2.0"

inherit autotools pkgconfig

PACKAGECONFIG ??= "${@bb.utils.filter('DISTRO_FEATURES', 'systemd', d)}"
PACKAGECONFIG[systemd] = ",,systemd"
