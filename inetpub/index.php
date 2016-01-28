<?php

  require_once('dbsession.php');
  require_once('tblcompleted.php');
  require_once('tblpending.php');
  require_once('tblconfig.php');

  session_start();

  $oSession = new CDatabaseSession();
  $oSession->ConnectDatabase("heating", "localhost", "heating", "toohottoocold");

  if(isset($_SESSION["home"])) {
    $sZones = $_SESSION["zones"];
    $nHome = $_SESSION["home"];
    $nPollEdit = $_SESSION["polledit"];
    $nTimeout = $_SESSION["timeout"];
    $nAlertEdit = $_SESSION["alertedit"];
    $nCondEdit = $_SESSION["condedit"];
    $nConfEdit = $_SESSION["confedit"];
    $nCmdEdit = $_SESSION["cmdedit"];
    $sRootTag = $_SESSION["roottag"];
    $nReport = $_SESSION["report"];
  }
  else {
    $oConfig = new CRowtblconfig();
    $oConfig->SetParam("ZONES");
    $oConfig->Select($oSession);
    $sZones = $oConfig->GetValue();
    $oConfig->Clear();
    $oConfig->SetParam("HOME");
    $oConfig->Select($oSession);
    $nHome = $oConfig->GetValue();
    $oConfig->Clear();
    $oConfig->SetParam("TIMEOUT");
    $oConfig->Select($oSession);
    $nTimeout = $oConfig->GetValue() / 1000;
    $oConfig->Clear();
    $oConfig->SetParam("POLLEDIT");
    $oConfig->Select($oSession);
    $nPollEdit = $oConfig->GetValue();
    $oConfig->Clear();
    $oConfig->SetParam("ALERTEDIT");
    $oConfig->Select($oSession);
    $nAlertEdit = $oConfig->GetValue();
    $oConfig->Clear();
    $oConfig->SetParam("CONDEDIT");
    $oConfig->Select($oSession);
    $nCondEdit = $oConfig->GetValue();
    $oConfig->Clear();
    $oConfig->SetParam("CMDEDIT");
    $oConfig->Select($oSession);
    $nCmdEdit = $oConfig->GetValue();
    $oConfig->Clear();
    $oConfig->SetParam("CONFEDIT");
    $oConfig->Select($oSession);
    $nConfEdit = $oConfig->GetValue();
    $oConfig->Clear();
    $oConfig->SetParam("SYSTEMTAG");
    $oConfig->Select($oSession);
    $sRootTag = $oConfig->GetValue();
    $oConfig->Clear();
    $oConfig->SetParam("REPORTS");
    $oConfig->Select($oSession);
    $nReport = $oConfig->GetValue();
    $nAccess = 2; //Non-admin access
    $_SESSION["zones"] = $sZones;
    $_SESSION["home"] = $nHome;
    $_SESSION["timeout"] = $nTimeout;
    $_SESSION["polledit"] = $nPollEdit;
    $_SESSION["alertedit"] = $nAlertEdit;
    $_SESSION["condedit"] = $nCondEdit;
    $_SESSION["cmdedit"] = $nCmdEdit;
    $_SESSION["confedit"] = $nConfEdit;
    $_SESSION["roottag"] = $sRootTag;
    $_SESSION["report"] = $nReport;
  }

  if(isset($_GET["_command"]))
    $nCommandGroup = $_GET["_command"];
  else
    $nCommandGroup = $nHome;

  $sFormat = "html";
  if(isset($_GET["_format"]))
    $sFormat = $_GET["_format"];
  else if(isset($_POST["_format"]))
    $sFormat = $_POST["_format"];
  else if(isset($_SESSION["_format"]))
    $sFormat = $_SESSION["_format"];

  $sTrans = "default";
  if($sFormat == "csv") {
    $sTrans = "csv";
    if(isset($_SESSION["_format"]))
      $sFormat = $_SESSION["_format"];
    else
      $sFormat = "html";
  }
  if($nCommandGroup != $nReport)
    $_SESSION["_format"] = $sFormat;


  $sXML = "";
  foreach($_POST as $sKey => $sValue)
    if(strncmp($sKey, "_", 1) != 0)
      $sXML .= "<arg id=\"" . trim($sKey) . "\">" . trim(htmlspecialchars($sValue)) . "</arg>";

  foreach($_GET as $sKey => $sValue)
    if(strncmp($sKey, "_", 1) != 0)
      if(!isset($_POST[$sKey]))
        $sXML .= "<arg id=\"" . trim($sKey) . "\">" . trim(htmlspecialchars($sValue)) . "</arg>";

  $sXML = "<args>" . $sXML . "</args>";
  $oPending = new CRowtblpending();
  $oPending->SetGroupId($nCommandGroup);
  $oPending->SetInTimestamp(date("Y-m-d H:i:s"));
  $oPending->SetInXML($sXML);
  $oPending->SetSource(0);
  $oPending->SetCompleted(2);
  $bError = !$oPending->Insert($oSession);

  if($bError)
    $sMessage = "<error><message>" . $oPending->GetErrorInfo() . "</message></error>";
  else {
    $nKey=$oPending->GetPendingId();
    $nSleep=0;
    do {
      if($nSleep > 0)
        sleep(1);
      $oPending->Clear();
      $oPending->SetPendingId($nKey);
      $oPending->Select($oSession);
      $nSleep++;
    } while(($nSleep < $nTimeout) && ($oPending->GetCompleted() != 1) && ($oPending->GetCompleted() != 4));

    $bError = (($nSleep == $nTimeout) && ($oPending->GetCompleted() != 1) && ($oPending->GetCompleted() != 4));    
    if($bError)
      $sMessage = "<error><message>Timeout</message></error>";
    else {
      $sMessage = "";
      $oCompleted = new CRowtblcompleted();
      $oCompleted->SetPendingId($nKey);
      $oCompleted->SetOrder("CompletedId");
      $oCompleted->Select($oSession);
      $sXML = "";
      while(!$oCompleted->GetEof()) {
        if($oCompleted->GetSuccess() == 0) {
          $sXML .= $oCompleted->GetOutXML();
          $sMode = $oCompleted->GetMode();
          if($sMode == "")
            $sMode = "default";
          $nIndexEnd = strpos($sXML, "/>");
          $nIndex = strpos($sXML, ">");
          if(($nIndexEnd !== FALSE) && ($nIndexEnd < $nIndex)) {
            $sXML = substr($sXML, 0, $nIndexEnd) . " mode=\"" . $sMode . "\" " . substr($sXML, $nIndexEnd);
          }
          else
          {
            $sXML = substr($sXML, 0, $nIndex) . " mode=\"" . $sMode . "\" val=\"$nIndex-$nIndexEnd\"" . substr($sXML, $nIndex);
          }
          $sMessage .= $sXML;
          $sXML = "";
        } else if($oCompleted->GetSuccess() == 5)
          $sXML .= $oCompleted->GetOutXML();
        else {
          $bError = true;
          $sXML .= $oCompleted->GetOutXML();
          $sMessage .= $sXML;
          $sXML = "";
        }
        $oCompleted->MoveNext();
      }
    }
  }

  if($nCommandGroup == $nConfEdit) {
    $oConfig = new CRowtblconfig();
    $oConfig->SetParam("ZONES");
    $oConfig->Select($oSession);
    $sZones = $oConfig->GetValue();
    $oConfig->Clear();
    $oConfig->SetParam("TIMEOUT");
    $oConfig->Select($oSession);
    $nTimeout = $oConfig->GetValue() / 1000;
    $_SESSION["timeout"] = $nTimeout;
    $_SESSION["zones"] = $sZones;
  }

  $sHeader = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>";
  if($sFormat == "xslt")
    $sHeader .= "<?xml-stylesheet type=\"text/xsl\" href=\"heating_" . $sTrans . ".xsl\" ?>";
  $sHeader .= "<" . $sRootTag . " current=\"" . $nCommandGroup . "\" home=\"" . $nHome . "\" polledit=\"" . $nPollEdit . "\" alertedit=\"" . $nAlertEdit . "\" conditionedit=\"" . $nCondEdit . "\" commandedit=\"" . $nCmdEdit . "\">";
  if(!$bError)
      $sHeader .= $sZones;
  $sMessage = $sHeader . $sMessage . "</" . $sRootTag . ">";
  if($sFormat == "html")
  {
    $oXml = new DOMDocument();
    $oXml->loadXML($sMessage);
    $oXsl = new DOMDocument();
    $oXsl->load("heating_" . $sTrans . ".xsl");
    $oProc = new XSLTProcessor();
    $oProc->importStyleSheet($oXsl);
    $sText = $oProc->transformToXML($oXml);
    if($sTrans == "csv")
    {
      header("Content-type: application/octet-stream");
      $sStart = $_POST["startday"] . "-" . $_POST["startmonth"] . "-" . $_POST["startyear"];
      if(strlen($sStart) == 2)
        $sStart = "Start";
      $sEnd = $_POST["endday"] . "-" . $_POST["endmonth"] . "-" . $_POST["endyear"];
      if(strlen($sEnd) == 2)
        $sEnd = date("j-n-Y");
      header("Content-Disposition: attachment; filename=\"data from $sStart to $sEnd.csv\"");
      header("Content-length: " + strlen($sText));
      header("Cache-control: private");
    }
    echo $sText;
  }
  else
  {
    header("Content-Type:text/xml");
    echo($sMessage);

  }

?>