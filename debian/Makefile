#
# This Makefile is responsoble for build debian package.
# 4 variables must be provided:
#   HSN2_VER=2.0
#   BUILD_NUMBER=999
#   DEBIAN_DIST=(experimental|2.0)

DEBIAN_DIST=experimental
HSN2_COMPONENT=swf-cve

all:	hsn2-swf-cve

clean:	hsn2-swf-cve-clean

PKG=hsn2-$(HSN2_COMPONENT)_$(HSN2_VER)-$(BUILD_NUMBER)_all
hsn2-swf-cve: hsn2-swf-cve-clean
	mkdir -p $(PKG)/DEBIAN
	mkdir -p $(PKG)/etc/init.d
	mkdir -p $(PKG)/opt/hsn2/$(HSN2_COMPONENT)
	tar -zxf ../target/hsn2-swf-cve-*.tar.gz -C $(PKG)/opt/hsn2/$(HSN2_COMPONENT)
	cp swf.initd $(PKG)/etc/init.d/hsn2-$(HSN2_COMPONENT)
	cp control ${PKG}/DEBIAN
	sed -i "s/{VER}/${HSN2_VER}-${BUILD_NUMBER}/" $(PKG)/DEBIAN/control
	sed -i "s/{DEBIAN_DIST}/${DEBIAN_DIST}/" $(PKG)/DEBIAN/control
	fakeroot dpkg -b $(PKG)

hsn2-swf-cve-clean:
	rm -rf hsn2-$(HSN2_COMPONENT)*
