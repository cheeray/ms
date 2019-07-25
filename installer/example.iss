;This file will be executed next to the application bundle image
;I.e. current directory will contain folder PricingService with application files
#ifdef INSTALL_TYPE
  #define InstallType INSTALL_TYPE
#else
  #define InstallType "test"
#endif
#ifndef INSTALL_DRIVE
  #define INSTALL_DRIVE "D:\"
#endif
[Setup]
AppId=msapp-{#InstallType}
AppName=msapp-example
AppVersion=0.0.1
AppVerName=MsAppExample 0.0.1
AppPublisher=cheeray
AppComments=Micro App example service
AppCopyright=Copyright (C) 2019
DefaultDirName={#INSTALL_DRIVE}\cheeray\msapp-{#InstallType}
DisableStartupPrompt=Yes
DisableDirPage=Yes
DisableProgramGroupPage=Yes
DisableReadyPage=Yes
DisableFinishedPage=Yes
DisableWelcomePage=Yes
DisableReadyMemo=Yes
DefaultGroupName=MsApp
;Optional License
LicenseFile=
;WinXP or above
MinVersion=0,5.1
OutputBaseFilename=msapp-0.0.1-{#InstallType}
Compression=lzma
SolidCompression=yes
PrivilegesRequired=admin
SetupIconFile=example.ico
SetupMutex=msapp-{#InstallType}
UninstallDisplayIcon={app}\example.ico
UninstallDisplayName=MsAppExample
WizardImageStretch=No
WizardSmallImageFile=example-setup-icon.bmp   
ArchitecturesInstallIn64BitMode=x64


[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Files]
Source: "prunsrv.exe"; DestDir: "{app}"; DestName: "MsAppExample.exe"; Flags: ignoreversion
Source: "bootstrap-{#InstallType}.yml"; DestDir: "{app}"; DestName: "bootstrap.yml"; Flags: ignoreversion
Source: "install-app-service.bat"; DestDir: "{app}"; DestName: "install.bat"; Flags: ignoreversion
Source: "..\target\example.jar"; DestDir: "{app}"; Flags: ignoreversion; 
Source: "..\target\bin\*"; DestDir: "{app}\bin\"; Flags: recursesubdirs; 
Source: "..\target\lib\*"; DestDir: "{app}\lib\"; Flags: recursesubdirs; 

[Dirs]
Name: "{app}\log"; Permissions: users-modify; Flags: uninsalwaysuninstall

[Icons]
Name: "{group}\example"; Filename: "{app}\MsAppExample.exe"; IconFilename: "{app}\example.ico"; Check: returnTrue()
Name: "{commondesktop}\MsAppExample"; Filename: "{app}\MsAppExample.exe";  IconFilename: "{app}\example.ico"; Check: returnFalse()

[Run]
Filename: "{app}\install.bat"; Flags: runhidden;  BeforeInstall: initProc; AfterInstall: checkServices

[UninstallRun]
Filename: "{app}\uninstall.bat"; Check: returnTrue()

[UninstallDelete]
Type: files; Name: "{app}\install.log"
Type: files; Name: "{app}\uninstall.bat"

[Code]
function returnTrue(): Boolean;
begin
  Result := True;
end;

function returnFalse(): Boolean;
begin
  Result := False;
end;

function InitializeSetup(): Boolean;
var
  UninstPath: String;
  RegKey: String;
  ResultCode: Integer;
begin
// Possible future improvements:
//   if version less or same => just launch app
//   if upgrade => check if same app is running and wait for it to exit
// Uninstall previous version
  RegKey := ExpandConstant('Software\Microsoft\Windows\CurrentVersion\Uninstall\{#emit SetupSetting("AppId")}_is1');
  if RegQueryStringValue(HKEY_LOCAL_MACHINE, RegKey,'QuietUninstallString', UninstPath) then
  begin
    Exec('>',UninstPath, '', SW_SHOW, ewWaitUntilTerminated, ResultCode);
    if ResultCode <> 0  then
    begin
      MsgBox('Failed remove previous version, please remove manually before install: ' + SysErrorMessage(ResultCode), mbError, MB_OK);
      Abort;
    end;
  end;
  // Check drive
  if not DirExists('{#INSTALL_DRIVE}') then
  begin
    MsgBox('{#INSTALL_DRIVE} drive not found. Abort.', mbError, MB_OK);
    Abort;
  end;
  Result := TRUE;
end;   

function changeYaml(const FileName: string; const HostName: string): Boolean;
begin
  Result := SaveStringToFile(FileName, #13#10 +'        data-key: ' + HostName + #13#10, true);
end;

procedure initProc();
begin
    if not changeYaml(ExpandConstant('{app}\bootstrap.yml'), GetComputerNameString()) then
    MsgBox('Failed to config MsAppExample, please config "data-key" in "bootstrap.yml" on Consul manually!', mbError, MB_OK);
end;

procedure checkServices();
var
  log: string;
  S: String;
begin
  log := ExpandConstant('{app}\install.log');
  if FileExists(log) then begin
    if LoadStringFromFile(log, S) then begin
      if Length(S) = 6 then begin
        MsgBox('No services been installed. Please check Data folders in {#INSTALL_DRIVE}.', mbError, MB_OK);
        Abort;
      end
    end
  end
  else begin
    MsgBox('Failed install services.', mbError, MB_OK);
    Abort;
  end
end;