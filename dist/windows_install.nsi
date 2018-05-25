# NSIS installation script for Hale

# general

Unicode true
SetCompressor /SOLID lzma

!define APPNAME "Hale"
!define VERSION "0.7.1"
!define ICON "logo.ico"

# name of the installer
Name "${APPNAME}"
Caption "${APPNAME} ${VERSION} Setup"

Icon "${ICON}"

BrandingText " "

# installer file name
OutFile "..\build\${APPNAME}-${VERSION}-setup.exe"

# default installation directory^
InstallDir "$PROGRAMFILES64\${APPNAME}"

# pages

Page directory
Page instfiles

UninstPage uninstConfirm
UninstPage instfiles

Section "install"

  SetOutPath "$INSTDIR"
  
  # create start menu entries
  CreateDirectory "$SMPROGRAMS\${APPNAME}"
  CreateShortcut "$SMPROGRAMS\${APPNAME}\Start ${APPNAME}.lnk" "$INSTDIR\start.bat" "" "${ICON}"
  
  # all files in distribution folder
  File /r "..\build\dist\*"
  
  WriteUninstaller uninst.exe
  
  # TODO registry information for add/remove programs (see http://nsis.sourceforge.net/A_simple_installer_with_start_menu_shortcut_and_uninstaller)

SectionEnd

Section "Uninstall"

  # remove start menu entries
  RmDir /r "$SMPROGRAMS\${APPNAME}"

  # remove files
  RMDir /r "$INSTDIR"
  
  # TODO remove uninstaller information from the registry

SectionEnd