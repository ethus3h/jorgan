; Script generated by the Inno Setup Script Wizard.
; SEE THE DOCUMENTATION FOR DETAILS ON CREATING INNO SETUP SCRIPT FILES!

[Setup]
; NOTE: The value of AppId uniquely identifies this application.
; Do not use the same AppId value in installers for other applications.
; (To generate a new GUID, click Tools | Generate GUID inside the IDE.)
AppId={{68AF851E-E65E-4DCD-8428-37E2D93E3A56}
AppName=jOrgan
AppPublisher=Sven Meier
AppPublisherURL=http://jorgan.sourceforge.net
AppSupportURL=http://jorgan.sourceforge.net
AppUpdatesURL=http://jorgan.sourceforge.net
AppVerName=jOrgan 3.5.1
OutputBaseFilename=jOrgan-3.5.1-installer1
OutputDir=.\target
DefaultDirName={pf}\jOrgan
DefaultGroupName=jOrgan
DisableProgramGroupPage=yes
LicenseFile=..\core\docs\license.txt
SetupIconFile=.\src\jorgan.ico
Compression=lzma
SolidCompression=yes

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Types]
Name: "full"; Description: "Full installation"
Name: "custom"; Description: "Custom installation"; Flags: iscustom

[Components]
Name: "core"; Description: "Program Files"; Types: full custom; Flags: fixed
Name: "creative"; Description: "Creative Soundblaster"; Types: full
Name: "fluidsynth"; Description: "Fluidsynth Sampler"; Types: full
Name: "linuxsampler"; Description: "Linuxsampler"; Types: full
Name: "skins"; Description: "Skins"; Types: full

[Files]
Source: ".\target\jOrgan.exe"; DestDir: "{app}"; Components: core
Source: "..\core\target\marshal\*"; DestDir: "{app}"; Components: core; Flags: recursesubdirs createallsubdirs
Source: "..\creative\target\marshal\*"; DestDir: "{app}"; Components: creative; Flags: recursesubdirs createallsubdirs
Source: "..\fluidsynth\target\marshal\*"; DestDir: "{app}"; Components: fluidsynth; Flags: recursesubdirs createallsubdirs
Source: "..\linuxsampler\target\marshal\*"; DestDir: "{app}"; Components: linuxsampler; Flags: recursesubdirs createallsubdirs
Source: "..\skins\target\marshal\*"; DestDir: "{app}"; Components: skins; Flags: recursesubdirs createallsubdirs

[Icons]
Name: "{group}\jOrgan"; Filename: "{app}\jOrgan.exe"
Name: "{group}\{cm:ProgramOnTheWeb,jOrgan}"; Filename: "http://jorgan.sourceforge.net"
Name: "{group}\{cm:UninstallProgram,jOrgan}"; Filename: "{uninstallexe}"
Name: "{commondesktop}\jOrgan"; Filename: "{app}\jOrgan.exe"; Tasks: desktopicon

[Registry]
Root: HKLM; Subkey: "Software\Microsoft\Windows\CurrentVersion\App Paths\jOrgan.exe"; ValueType: string; ValueName: ""; ValueData: "{app}"; Flags: uninsdeletekey
Root: HKLM; Subkey: "Software\Microsoft\Windows\CurrentVersion\App Paths\jOrgan.exe"; ValueType: string; ValueName: "Path"; ValueData: "{app}\jOrgan.exe"; Flags: uninsdeletekey

[Run]
Filename: "{app}\jOrgan.exe"; Description: "{cm:LaunchProgram,jOrgan}"; Flags: nowait postinstall skipifsilent

