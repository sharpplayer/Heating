<?php

  require_once('dbo.php');

  class CRowtblcompleted extends CDatabaseObject
  {
    var $m_sTABLE_NAME="tblcompleted";

    var $m_nCompletedId;
    var $m_nPendingId;
    var $m_nCommandId;
    var $m_sInXML;
    var $m_sOutXML;
    var $m_dInTimestamp;
    var $m_dExecTimestamp;
    var $m_nSource;
    var $m_nSuccess;
    var $m_sMode;
    var $m_bData;


    function GetCompletedId(){ if(isset($this->m_nCompletedId)) return($this->m_nCompletedId);}
    function SetCompletedId($value){$this->m_nCompletedId=$value;}
    function ClearCompletedId(){ unset($this->m_nCompletedId);}
    function GetPendingId(){ if(isset($this->m_nPendingId)) return($this->m_nPendingId);}
    function SetPendingId($value){$this->m_nPendingId=$value;}
    function ClearPendingId(){ unset($this->m_nPendingId);}
    function GetCommandId(){ if(isset($this->m_nCommandId)) return($this->m_nCommandId);}
    function SetCommandId($value){$this->m_nCommandId=$value;}
    function ClearCommandId(){ unset($this->m_nCommandId);}
    function GetInXML(){ if(isset($this->m_sInXML)) return($this->m_sInXML);}
    function GetInXMLHTML(){ if(isset($this->m_sInXML)) return(htmlspecialchars($this->m_sInXML));}
    function SetInXML($value){$this->m_sInXML=$value;}
    function ClearInXML(){ unset($this->m_sInXML);}
    function GetOutXML(){ if(isset($this->m_sOutXML)) return($this->m_sOutXML);}
    function GetOutXMLHTML(){ if(isset($this->m_sOutXML)) return(htmlspecialchars($this->m_sOutXML));}
    function SetOutXML($value){$this->m_sOutXML=$value;}
    function ClearOutXML(){ unset($this->m_sOutXML);}
    function GetInTimestamp(){ if(isset($this->m_dInTimestamp)) return($this->m_dInTimestamp);}
    function SetInTimestamp($value){$this->m_dInTimestamp=$value;}
    function ClearInTimestamp(){ unset($this->m_dInTimestamp);}
    function GetExecTimestamp(){ if(isset($this->m_dExecTimestamp)) return($this->m_dExecTimestamp);}
    function SetExecTimestamp($value){$this->m_dExecTimestamp=$value;}
    function ClearExecTimestamp(){ unset($this->m_dExecTimestamp);}
    function GetSource(){ if(isset($this->m_nSource)) return($this->m_nSource);}
    function SetSource($value){$this->m_nSource=$value;}
    function ClearSource(){ unset($this->m_nSource);}
    function GetSuccess(){ if(isset($this->m_nSuccess)) return($this->m_nSuccess);}
    function SetSuccess($value){$this->m_nSuccess=$value;}
    function ClearSuccess(){ unset($this->m_nSuccess);}
    function GetMode(){ if(isset($this->m_sMode)) return($this->m_sMode);}
    function GetModeHTML(){ if(isset($this->m_sMode)) return(htmlspecialchars($this->m_sMode));}
    function SetMode($value){$this->m_sMode=$value;}
    function ClearMode(){ unset($this->m_sMode);}
    function GetData(){ if(isset($this->m_bData)) return($this->m_bData);}
    function SetData($value){$this->m_bData=$value;}
    function ClearData(){ unset($this->m_bData);}


    function Clear(){
      unset($this->m_nCompletedId);
      unset($this->m_nPendingId);
      unset($this->m_nCommandId);
      unset($this->m_sInXML);
      unset($this->m_sOutXML);
      unset($this->m_dInTimestamp);
      unset($this->m_dExecTimestamp);
      unset($this->m_nSource);
      unset($this->m_nSuccess);
      unset($this->m_sMode);
      unset($this->m_bData);
      $this->m_nFromRecord = 0;
      $this->m_nMaxRecords = -1;
    }
    function Fill(){
      if(isset($this->m_oTable)){
        $oRow = current($this->m_oTable);
        if($oRow != false){
          $this->Clear();
          $this->m_nCompletedId = $oRow->GetCompletedId();
          $this->m_nPendingId = $oRow->GetPendingId();
          $this->m_nCommandId = $oRow->GetCommandId();
          $this->m_sInXML = $oRow->GetInXML();
          $this->m_sOutXML = $oRow->GetOutXML();
          $this->m_dInTimestamp = $oRow->GetInTimestamp();
          $this->m_dExecTimestamp = $oRow->GetExecTimestamp();
          $this->m_nSource = $oRow->GetSource();
          $this->m_nSuccess = $oRow->GetSuccess();
          $this->m_sMode = $oRow->GetMode();
          $this->m_bData = $oRow->GetData();
        }
      }
    }
    function WriteFile($p_oSession, $p_bAppend){
      if($p_bAppend){
        $oFile = @fopen($p_oSession->GetDatabase() . $this->m_sTABLE_NAME . ".dat", "r+");
        if($oFile == 0) {
          $this->m_nError = $this->m_nDBO_BAD_READ_FILE;
          $this->m_sErrorInfo = $this->m_sTABLE_NAME . ".dat";
          return(false);
        }
        $sData = "";
        while(!feof($oFile))
          $this->ReadRecord($oFile, $p_oSession, $sData);
        if($sData == "")
          $this->m_nCompletedId = 1;
        else
          $this->m_nCompletedId = $sData[0] + 1;
        $sData = array();
        $sData[0] = $this->m_nCompletedId;
        $sData[1] = $this->m_nPendingId;
        $sData[2] = $this->m_nCommandId;
        $sData[3] = $this->m_sInXML;
        $sData[4] = $this->m_sOutXML;
        $sData[5] = $this->m_dInTimestamp;
        $sData[6] = $this->m_dExecTimestamp;
        $sData[7] = $this->m_nSource;
        $sData[8] = $this->m_nSuccess;
        $sData[9] = $this->m_sMode;
        $sData[10] = $this->m_bData;
        fputs($oFile, $p_oSession->GetSafeRecord($sData, true) . "\r\n");
        fclose($oFile);
        return(true);
      }
      else{
        $oFile = @fopen($p_oSession->GetDatabase() . $this->m_sTABLE_NAME . ".dat", "w");
        if($oFile == 0) {
          $this->m_nError = $this->m_nDBO_BAD_WRITE_FILE;
          $this->m_sErrorInfo = $this->m_sTABLE_NAME . ".dat";
          return(false);
        }
        reset($this->m_oTable);
        while(list(,$oRow) = each($this->m_oTable)){
          $sData = array();
          $sData[0] = $oRow->GetCompletedId();
          $sData[1] = $oRow->GetPendingId();
          $sData[2] = $oRow->GetCommandId();
          $sData[3] = $oRow->GetInXML();
          $sData[4] = $oRow->GetOutXML();
          $sData[5] = $oRow->GetInTimestamp();
          $sData[6] = $oRow->GetExecTimestamp();
          $sData[7] = $oRow->GetSource();
          $sData[8] = $oRow->GetSuccess();
          $sData[9] = $oRow->GetMode();
          $sData[10] = $oRow->GetData();
          fputs($oFile, $p_oSession->GetSafeRecord($sData, true) . "\r\n");
        }
      }
      fclose($oFile);
      return(true);
    }
    function MatchCriteria($p_sData, $p_bKey){
      if(isset($this->m_nCompletedId))
        if($this->m_nCompletedId != $p_sData[0])
          return(false);
      if($p_bKey)
        return(true);
      if(isset($this->m_nPendingId))
        if($this->m_nPendingId != $p_sData[1])
          return(false);
      if(isset($this->m_nCommandId))
        if($this->m_nCommandId != $p_sData[2])
          return(false);
      if(isset($this->m_sInXML))
        if($this->m_sInXML != $p_sData[3])
          return(false);
      if(isset($this->m_sOutXML))
        if($this->m_sOutXML != $p_sData[4])
          return(false);
      if(isset($this->m_dInTimestamp))
        if($this->m_dInTimestamp != $p_sData[5])
          return(false);
      if(isset($this->m_dExecTimestamp))
        if($this->m_dExecTimestamp != $p_sData[6])
          return(false);
      if(isset($this->m_nSource))
        if($this->m_nSource != $p_sData[7])
          return(false);
      if(isset($this->m_nSuccess))
        if($this->m_nSuccess != $p_sData[8])
          return(false);
      if(isset($this->m_sMode))
        if($this->m_sMode != $p_sData[9])
          return(false);
      if(isset($this->m_bData))
        if($this->m_bData != $p_sData[10])
          return(false);
      return(true);
    }
    function GenerateWhereClause($p_oSession, $p_bKeys, &$p_sSql){
      $bKeySet=$p_bKeys;
      $p_sSql="";
      $sConj=" WHERE ";
      if(isset($this->m_nCompletedId)){
        if(is_numeric($this->m_nCompletedId))
          $p_sSql .= $sConj . "CompletedId = " . $this->m_nCompletedId;
        else
          return(false);
        $sConj = " AND ";
      }
      else 
        $bKeySet=false;
      if($bKeySet)
        return(true);
      if(strlen($this->m_sFilter)){
        $p_sSql .= $sConj . $this->m_sFilter;
        $sConj=" AND ";
      }
      if(isset($this->m_nPendingId)){
        if(is_numeric($this->m_nPendingId))
          $p_sSql .= $sConj . "PendingId = " . $this->m_nPendingId;
        else
          return(false);
        $sConj = " AND ";
      }
      if(isset($this->m_nCommandId)){
        if(is_numeric($this->m_nCommandId))
          $p_sSql .= $sConj . "CommandId = " . $this->m_nCommandId;
        else
          return(false);
        $sConj = " AND ";
      }
      if(isset($this->m_sInXML)){
        if($this->m_bWildcards){
          if(strcspn($this->m_sInXML, "_%") == strlen($this->m_sInXML))
            $p_sSql .= $sConj . "InXML = '" . $p_oSession->GetSafeSQL($this->m_sInXML) . "'";
          else
            $p_sSql .= $sConj . "InXML LIKE '" . $p_oSession->GetSafeSQL($this->m_sInXML) . "'";
        }
        else
          $p_sSql .= $sConj . "InXML = '" . $p_oSession->GetSafeSQL($this->m_sInXML) . "'";
        $sConj = " AND ";
      }
      if(isset($this->m_sOutXML)){
        if($this->m_bWildcards){
          if(strcspn($this->m_sOutXML, "_%") == strlen($this->m_sOutXML))
            $p_sSql .= $sConj . "OutXML = '" . $p_oSession->GetSafeSQL($this->m_sOutXML) . "'";
          else
            $p_sSql .= $sConj . "OutXML LIKE '" . $p_oSession->GetSafeSQL($this->m_sOutXML) . "'";
        }
        else
          $p_sSql .= $sConj . "OutXML = '" . $p_oSession->GetSafeSQL($this->m_sOutXML) . "'";
        $sConj = " AND ";
      }
      if(isset($this->m_dInTimestamp)){
        $p_sSql .= $sConj . "InTimestamp = '" . $this->m_dInTimestamp . "'";
        $sConj = " AND ";
      }
      if(isset($this->m_dExecTimestamp)){
        $p_sSql .= $sConj . "ExecTimestamp = '" . $this->m_dExecTimestamp . "'";
        $sConj = " AND ";
      }
      if(isset($this->m_nSource)){
        if(is_numeric($this->m_nSource))
          $p_sSql .= $sConj . "Source = " . $this->m_nSource;
        else
          return(false);
        $sConj = " AND ";
      }
      if(isset($this->m_nSuccess)){
        if(is_numeric($this->m_nSuccess))
          $p_sSql .= $sConj . "Success = " . $this->m_nSuccess;
        else
          return(false);
        $sConj = " AND ";
      }
      if(isset($this->m_sMode)){
        if($this->m_bWildcards){
          if(strcspn($this->m_sMode, "_%") == strlen($this->m_sMode))
            $p_sSql .= $sConj . "Mode = '" . $p_oSession->GetSafeSQL($this->m_sMode) . "'";
          else
            $p_sSql .= $sConj . "Mode LIKE '" . $p_oSession->GetSafeSQL($this->m_sMode) . "'";
        }
        else
          $p_sSql .= $sConj . "Mode = '" . $p_oSession->GetSafeSQL($this->m_sMode) . "'";
        $sConj = " AND ";
      }
      if(isset($this->m_bData)){
        $p_sSql .= $sConj . "Data = " . $this->GetBool($this->m_bData) . "";
        $sConj = " AND ";
      }
      return(true);
    }
    function Delete($p_oSession){
      $this->m_nError = $this->m_nDBO_OK;
      if($p_oSession->GetFileRead()){
        $bFound = false;
        $this->m_oTable = array();
        $oFile = @fopen($p_oSession->GetDatabase() . $this->m_sTABLE_NAME . ".dat", "r");
        if($oFile == 0) {
          $this->m_nError = $this->m_nDBO_BAD_READ_FILE;
          $this->m_sErrorInfo = $this->m_sTABLE_NAME . ".dat";
          return(false);
        }
        while(!feof($oFile)){
          if($this->ReadRecord($oFile, $p_oSession, $sData)){
            if(!$this->MatchCriteria($sData, true)){
              $oObj = new CRowtblcompleted();
              if($sData[0] != "")
                $oObj->SetCompletedId($sData[0]);
              if($sData[1] != "")
                $oObj->SetPendingId($sData[1]);
              if($sData[2] != "")
                $oObj->SetCommandId($sData[2]);
              if($sData[3] != "")
                $oObj->SetInXML($sData[3]);
              if($sData[4] != "")
                $oObj->SetOutXML($sData[4]);
              if($sData[5] != "")
                $oObj->SetInTimestamp($sData[5]);
              if($sData[6] != "")
                $oObj->SetExecTimestamp($sData[6]);
              if($sData[7] != "")
                $oObj->SetSource($sData[7]);
              if($sData[8] != "")
                $oObj->SetSuccess($sData[8]);
              if($sData[9] != "")
                $oObj->SetMode($sData[9]);
              if($sData[10] != "")
                $oObj->SetData($sData[10]);
              array_push($this->m_oTable,$oObj);
            }
            else
              $bFound = true;
          }
        }
        fclose($oFile);
        if($bFound)
          return($this->WriteFile($p_oSession, false));
        else
          return(true);
      }
      else{
        if(!$this->GenerateWhereClause($p_oSession, true, $sWhere)){
          $this->m_nError = $this->m_nDBO_DATATYPE;
          $this->m_sErrorInfo = $sWhere;
          return(false);
        }
        $sSql="DELETE FROM " . $this->m_sTABLE_NAME . $sWhere;
        if(!$this->CheckSession($p_oSession)){
          $this->m_nError = $this->m_nDBO_BAD_SESSION;
          return(false);
        }
        else if(!mysql_query($sSql, $p_oSession->GetLink())) {
          $this->m_nError = $this->m_nDBO_DELETE_FAILURE;
          $this->m_sErrorInfo = mysql_error() . ":" . $sSql;
          return(false);
        }
        else
          return(true);
      }
    }
    function Update($p_oSession, $p_oFilter = ""){
      $this->m_nError = $this->m_nDBO_OK;
      if($p_oSession->GetFileRead()){
        $bFound = false;
        $this->m_oTable = array();
        $oFile = @fopen($p_oSession->GetDatabase() . $this->m_sTABLE_NAME . ".dat", "r");
        if($oFile == 0) {
          $this->m_nError = $this->m_nDBO_BAD_READ_FILE;
          $this->m_sErrorInfo = $this->m_sTABLE_NAME . ".dat";
          return(false);
        }
        while(!feof($oFile)){
          if($this->ReadRecord($oFile, $p_oSession, $sData)){
            $oObj = new CRowtblcompleted();
            if(!$this->MatchCriteria($sData, true)){
              if($sData[0] != "")
                $oObj->SetCompletedId($sData[0]);
              if($sData[1] != "")
                $oObj->SetPendingId($sData[1]);
              if($sData[2] != "")
                $oObj->SetCommandId($sData[2]);
              if($sData[3] != "")
                $oObj->SetInXML($sData[3]);
              if($sData[4] != "")
                $oObj->SetOutXML($sData[4]);
              if($sData[5] != "")
                $oObj->SetInTimestamp($sData[5]);
              if($sData[6] != "")
                $oObj->SetExecTimestamp($sData[6]);
              if($sData[7] != "")
                $oObj->SetSource($sData[7]);
              if($sData[8] != "")
                $oObj->SetSuccess($sData[8]);
              if($sData[9] != "")
                $oObj->SetMode($sData[9]);
              if($sData[10] != "")
                $oObj->SetData($sData[10]);
            }
            else{
              $bFound = true;
              if(isset($this->m_nCompletedId))
                $oObj->SetCompletedId($this->GetCompletedId());
              else
                $oObj->SetCompletedId($sData[0]);
              if(isset($this->m_nPendingId))
                $oObj->SetPendingId($this->GetPendingId());
              else
                $oObj->SetPendingId($sData[1]);
              if(isset($this->m_nCommandId))
                $oObj->SetCommandId($this->GetCommandId());
              else
                $oObj->SetCommandId($sData[2]);
              if(isset($this->m_sInXML))
                $oObj->SetInXML($this->GetInXML());
              else
                $oObj->SetInXML($sData[3]);
              if(isset($this->m_sOutXML))
                $oObj->SetOutXML($this->GetOutXML());
              else
                $oObj->SetOutXML($sData[4]);
              if(isset($this->m_dInTimestamp))
                $oObj->SetInTimestamp($this->GetInTimestamp());
              else
                $oObj->SetInTimestamp($sData[5]);
              if(isset($this->m_dExecTimestamp))
                $oObj->SetExecTimestamp($this->GetExecTimestamp());
              else
                $oObj->SetExecTimestamp($sData[6]);
              if(isset($this->m_nSource))
                $oObj->SetSource($this->GetSource());
              else
                $oObj->SetSource($sData[7]);
              if(isset($this->m_nSuccess))
                $oObj->SetSuccess($this->GetSuccess());
              else
                $oObj->SetSuccess($sData[8]);
              if(isset($this->m_sMode))
                $oObj->SetMode($this->GetMode());
              else
                $oObj->SetMode($sData[9]);
              if(isset($this->m_bData))
                $oObj->SetData($this->GetData());
              else
                $oObj->SetData($sData[10]);
            }
            array_push($this->m_oTable,$oObj);
          }
        }
        fclose($oFile);
        if($bFound)
          return($this->WriteFile($p_oSession, false));
        else
          return(true);
      }
      else{
        $sSql="UPDATE " . $this->m_sTABLE_NAME . " SET";
        $sConj = " ";
        if(isset($this->m_nPendingId)){
          if(is_numeric($this->m_nPendingId))
            $sSql .= $sConj . "PendingId = " . $this->m_nPendingId;
          else{
            $this->m_nError = $this->m_nDBO_DATATYPE;
            $this->m_sErrorInfo = "PendingId = " . $this->m_nPendingId;
            return(false);
          }
          $sConj = ",";
        }
        if(isset($this->m_nCommandId)){
          if(is_numeric($this->m_nCommandId))
            $sSql .= $sConj . "CommandId = " . $this->m_nCommandId;
          else{
            $this->m_nError = $this->m_nDBO_DATATYPE;
            $this->m_sErrorInfo = "CommandId = " . $this->m_nCommandId;
            return(false);
          }
          $sConj = ",";
        }
        if(isset($this->m_sInXML)){
          $sSql .= $sConj . "InXML = '" . $p_oSession->GetSafeSQL($this->m_sInXML) . "'";
          $sConj = ",";
        }
        if(isset($this->m_sOutXML)){
          $sSql .= $sConj . "OutXML = '" . $p_oSession->GetSafeSQL($this->m_sOutXML) . "'";
          $sConj = ",";
        }
        if(isset($this->m_dInTimestamp)){
          $sSql .= $sConj . "InTimestamp = '" . $this->m_dInTimestamp . "'";
          $sConj = ",";
        }
        if(isset($this->m_dExecTimestamp)){
          $sSql .= $sConj . "ExecTimestamp = '" . $this->m_dExecTimestamp . "'";
          $sConj = ",";
        }
        if(isset($this->m_nSource)){
          if(is_numeric($this->m_nSource))
            $sSql .= $sConj . "Source = " . $this->m_nSource;
          else{
            $this->m_nError = $this->m_nDBO_DATATYPE;
            $this->m_sErrorInfo = "Source = " . $this->m_nSource;
            return(false);
          }
          $sConj = ",";
        }
        if(isset($this->m_nSuccess)){
          if(is_numeric($this->m_nSuccess))
            $sSql .= $sConj . "Success = " . $this->m_nSuccess;
          else{
            $this->m_nError = $this->m_nDBO_DATATYPE;
            $this->m_sErrorInfo = "Success = " . $this->m_nSuccess;
            return(false);
          }
          $sConj = ",";
        }
        if(isset($this->m_sMode)){
          $sSql .= $sConj . "Mode = '" . $p_oSession->GetSafeSQL($this->m_sMode) . "'";
          $sConj = ",";
        }
        if(isset($this->m_bData)){
          $sSql .= $sConj . "Data = " . $this->GetBool($this->m_bData);
          $sConj = ",";
        }
        if(is_object($p_oFilter)){
          if($p_oFilter->GenerateWhereClause($p_oSession, true, $sWhere))
            $sSql .= $sWhere;
          else{
            $this->m_nError = $this->m_nDBO_DATATYPE;
            $this->m_sErrorInfo = $sSql . $sWhere;
            return(false);
          }
        }
        else{
          if($this->GenerateWhereClause($p_oSession, true, $sWhere))
            $sSql .= $sWhere;
          else{
            $this->m_nError = $this->m_nDBO_DATATYPE;
            $this->m_sErrorInfo = $sSql . $sWhere;
            return(false);
          }
        }
        if(!$this->CheckSession($p_oSession)){
          $this->m_nError = $this->m_nDBO_BAD_SESSION;
          return(false);
        }
        else if(!mysql_query($sSql, $p_oSession->GetLink())) {
          $this->m_nError = $this->m_nDBO_UPDATE_FAILURE;
          $this->m_sErrorInfo = mysql_error() . ":" . $sSql;
          return(false);
        }
        else
          return(true);
      }
    }
    function Insert($p_oSession){
      $this->m_nError = $this->m_nDBO_OK;
      if($p_oSession->GetFileRead()){
        return($this->WriteFile($p_oSession, true));
      }
      else{
        $sConj = " ";
        $sCols = "";
        $sValues = "";
        if(isset($this->m_nCompletedId)){
          $sCols .= $sConj . "CompletedId";
          if(is_numeric($this->m_nCompletedId))
            $sValues .= $sConj . $this->m_nCompletedId;
          else{
            $this->m_nError = $this->m_nDBO_DATATYPE;
            $this->m_sErrorInfo = "CompletedId = " . $this->m_nCompletedId;
            return(false);
          }
          $sConj = ",";
        }
        if(isset($this->m_nPendingId)){
          $sCols .= $sConj . "PendingId";
          if(is_numeric($this->m_nPendingId))
            $sValues .= $sConj . $this->m_nPendingId;
          else{
            $this->m_nError = $this->m_nDBO_DATATYPE;
            $this->m_sErrorInfo = "PendingId = " . $this->m_nPendingId;
            return(false);
          }
          $sConj = ",";
        }
        if(isset($this->m_nCommandId)){
          $sCols .= $sConj . "CommandId";
          if(is_numeric($this->m_nCommandId))
            $sValues .= $sConj . $this->m_nCommandId;
          else{
            $this->m_nError = $this->m_nDBO_DATATYPE;
            $this->m_sErrorInfo = "CommandId = " . $this->m_nCommandId;
            return(false);
          }
          $sConj = ",";
        }
        if(isset($this->m_sInXML)){
          $sCols .= $sConj . "InXML";
          $sValues .= $sConj . "'" . $p_oSession->GetSafeSQL($this->m_sInXML) . "'";
          $sConj = ",";
        }
        if(isset($this->m_sOutXML)){
          $sCols .= $sConj . "OutXML";
          $sValues .= $sConj . "'" . $p_oSession->GetSafeSQL($this->m_sOutXML) . "'";
          $sConj = ",";
        }
        if(isset($this->m_dInTimestamp)){
          $sCols .= $sConj . "InTimestamp";
          $sValues .= $sConj . "'" . $p_oSession->GetSafeSQL($this->m_dInTimestamp) . "'";
          $sConj = ",";
        }
        if(isset($this->m_dExecTimestamp)){
          $sCols .= $sConj . "ExecTimestamp";
          $sValues .= $sConj . "'" . $p_oSession->GetSafeSQL($this->m_dExecTimestamp) . "'";
          $sConj = ",";
        }
        if(isset($this->m_nSource)){
          $sCols .= $sConj . "Source";
          if(is_numeric($this->m_nSource))
            $sValues .= $sConj . $this->m_nSource;
          else{
            $this->m_nError = $this->m_nDBO_DATATYPE;
            $this->m_sErrorInfo = "Source = " . $this->m_nSource;
            return(false);
          }
          $sConj = ",";
        }
        if(isset($this->m_nSuccess)){
          $sCols .= $sConj . "Success";
          if(is_numeric($this->m_nSuccess))
            $sValues .= $sConj . $this->m_nSuccess;
          else{
            $this->m_nError = $this->m_nDBO_DATATYPE;
            $this->m_sErrorInfo = "Success = " . $this->m_nSuccess;
            return(false);
          }
          $sConj = ",";
        }
        if(isset($this->m_sMode)){
          $sCols .= $sConj . "Mode";
          $sValues .= $sConj . "'" . $p_oSession->GetSafeSQL($this->m_sMode) . "'";
          $sConj = ",";
        }
        if(isset($this->m_bData)){
          $sCols .= $sConj . "Data";
          $sValues .= $sConj . $this->GetBool($this->m_bData);
          $sConj = ",";
        }
        $sSql="INSERT INTO " . $this->m_sTABLE_NAME . " ($sCols) VALUES($sValues)";
        if(!$this->CheckSession($p_oSession)){
          $this->m_nError = $this->m_nDBO_BAD_SESSION;
          return(false);
        }
        else if(!mysql_query($sSql, $p_oSession->GetLink())) {
          $this->m_nError = $this->m_nDBO_INSERT_FAILURE;
          $this->m_sErrorInfo = mysql_error() . ":" . $sSql;
          return(false);
        }
        else if(!isset($this->m_nCompletedId))
          $this->m_nCompletedId= mysql_insert_id();
        return(true);
      }
    }
    function Select($p_oSession){
      $this->m_nError = $this->m_nDBO_OK;
      $bFirst = true;
      $this->m_bBof = true;
      $this->m_bEof = true;
      if($p_oSession->GetFileRead()){
        $oFile = @fopen($p_oSession->GetDatabase() . $this->m_sTABLE_NAME . ".dat", "r");
        if($oFile == 0) {
          $this->m_nError = $this->m_nDBO_BAD_READ_FILE;
          $this->m_sErrorInfo = $this->m_sTABLE_NAME . ".dat";
          return(false);
        }
        while(!feof($oFile)){
          if($this->ReadRecord($oFile, $p_oSession, $sData)){
            if($this->MatchCriteria($sData, false)){
              if($bFirst){
                $this->m_oTable = array();
                $bFirst = false;
                $this->m_bBof = false;
                $this->m_bEof = false;
              }
              $oObj = new CRowtblcompleted();
              if($sData[0] != "")
                $oObj->SetCompletedId($sData[0]);
              if($sData[1] != "")
                $oObj->SetPendingId($sData[1]);
              if($sData[2] != "")
                $oObj->SetCommandId($sData[2]);
              if($sData[3] != "")
                $oObj->SetInXML($sData[3]);
              if($sData[4] != "")
                $oObj->SetOutXML($sData[4]);
              if($sData[5] != "")
                $oObj->SetInTimestamp($sData[5]);
              if($sData[6] != "")
                $oObj->SetExecTimestamp($sData[6]);
              if($sData[7] != "")
                $oObj->SetSource($sData[7]);
              if($sData[8] != "")
                $oObj->SetSuccess($sData[8]);
              if($sData[9] != "")
                $oObj->SetMode($sData[9]);
              if($sData[10] != "")
                $oObj->SetData($sData[10]);
              array_push($this->m_oTable,$oObj);
            }
          }
        }
        if(!$bFirst){
          reset($this->m_oTable);
          $oRow = current($this->m_oTable);
          if($sData[0] != "")
            $this->m_nCompletedId = $oRow->GetCompletedId();
          if($sData[1] != "")
            $this->m_nPendingId = $oRow->GetPendingId();
          if($sData[2] != "")
            $this->m_nCommandId = $oRow->GetCommandId();
          if($sData[3] != "")
            $this->m_sInXML = $oRow->GetInXML();
          if($sData[4] != "")
            $this->m_sOutXML = $oRow->GetOutXML();
          if($sData[5] != "")
            $this->m_dInTimestamp = $oRow->GetInTimestamp();
          if($sData[6] != "")
            $this->m_dExecTimestamp = $oRow->GetExecTimestamp();
          if($sData[7] != "")
            $this->m_nSource = $oRow->GetSource();
          if($sData[8] != "")
            $this->m_nSuccess = $oRow->GetSuccess();
          if($sData[9] != "")
            $this->m_sMode = $oRow->GetMode();
          if($sData[10] != "")
            $this->m_bData = $oRow->GetData();
        }
        fclose($oFile);
      }
      else{
        $sSql="SELECT * FROM " . $this->m_sTABLE_NAME;
        if($this->GenerateWhereClause($p_oSession, false, $sWhere))
          $sSql .= $sWhere;
        else{
          $this->m_nError = $this->m_nDBO_DATATYPE;
          $this->m_sErrorInfo = $sWhere;
          return(false);
        }
        if(strlen($this->m_sSortBy))
          $sSql .= " ORDER BY " . $this->m_sSortBy;
        if($this->m_nFromRecord!=0)
          $sSql .= " LIMIT " . $this->m_nFromRecord . "," . $this->m_nMaxRecords;
        else if($this->m_nMaxRecords!=-1)
          $sSql .= " LIMIT " . $this->m_nMaxRecords;
        if(!$this->CheckSession($p_oSession)){
          $this->m_nError = $this->m_nDBO_BAD_SESSION;
          return(false);
        }
        else if(!($oResult = mysql_query($sSql, $p_oSession->GetLink()))) {
          $this->m_nError = $this->m_nDBO_SELECT_FAILURE;
          $this->m_sErrorInfo = mysql_error() . ":" . $sSql;
          return(false);
        }
        else{
          while(($oRow = mysql_fetch_object($oResult))){
            $oObj = new CRowtblcompleted();
            if($bFirst){
              $this->m_oTable = array();
              if(!is_null($oRow->CompletedId))
                $this->m_nCompletedId = $oRow->CompletedId;
              if(!is_null($oRow->PendingId))
                $this->m_nPendingId = $oRow->PendingId;
              if(!is_null($oRow->CommandId))
                $this->m_nCommandId = $oRow->CommandId;
              if(!is_null($oRow->InXML))
                $this->m_sInXML = $oRow->InXML;
              if(!is_null($oRow->OutXML))
                $this->m_sOutXML = $oRow->OutXML;
              if(!is_null($oRow->InTimestamp))
                $this->m_dInTimestamp = $oRow->InTimestamp;
              if(!is_null($oRow->ExecTimestamp))
                $this->m_dExecTimestamp = $oRow->ExecTimestamp;
              if(!is_null($oRow->Source))
                $this->m_nSource = $oRow->Source;
              if(!is_null($oRow->Success))
                $this->m_nSuccess = $oRow->Success;
              if(!is_null($oRow->Mode))
                $this->m_sMode = $oRow->Mode;
              if(!is_null($oRow->Data))
                $this->m_bData = $oRow->Data;
              $bFirst=false;
              $this->m_bBof = false;
              $this->m_bEof = false;
            }
            if(!is_null($oRow->CompletedId))
              $oObj->SetCompletedId($oRow->CompletedId);
            if(!is_null($oRow->PendingId))
              $oObj->SetPendingId($oRow->PendingId);
            if(!is_null($oRow->CommandId))
              $oObj->SetCommandId($oRow->CommandId);
            if(!is_null($oRow->InXML))
              $oObj->SetInXML($oRow->InXML);
            if(!is_null($oRow->OutXML))
              $oObj->SetOutXML($oRow->OutXML);
            if(!is_null($oRow->InTimestamp))
              $oObj->SetInTimestamp($oRow->InTimestamp);
            if(!is_null($oRow->ExecTimestamp))
              $oObj->SetExecTimestamp($oRow->ExecTimestamp);
            if(!is_null($oRow->Source))
              $oObj->SetSource($oRow->Source);
            if(!is_null($oRow->Success))
              $oObj->SetSuccess($oRow->Success);
            if(!is_null($oRow->Mode))
              $oObj->SetMode($oRow->Mode);
            if(!is_null($oRow->Data))
              $oObj->SetData($oRow->Data);
            array_push($this->m_oTable,$oObj);
          }
          mysql_free_result($oResult);
        }
      }
      return(true);
    }
    function Synchronise($p_oMaster, $p_oNonMaster, $p_nSystemId = 0){
      $this->m_nError = $this->m_nDBO_OK;
      $p_oMaster->SetSynchronising(true);
      $p_oNonMaster->SetSynchronising(true);
      $sOrder = "COMPLETEDID";
      $this->SetOrder($sOrder);
      $this->Select($p_oMaster);
      $oNonMaster = new CRowtblcompleted();
      $oNonMaster->SetOrder($sOrder);
      $oNonMaster->Select($p_oNonMaster);
      while(!($this->GetEof() && $oNonMaster->GetEof())){
        $nCompare = 0;
        if($this->GetEof())
          $nCompare = 1;
        else if($oNonMaster->GetEof())
          $nCompare = -1;
        else if($this->m_nCompletedId>$oNonMaster->GetCompletedId())
          $nCompare = 1;
        else if($this->m_nCompletedId<$oNonMaster->GetCompletedId())
          $nCompare = -1;
        if($nCompare==0){
          $nUpdate = 0;
          if($nUpdate != 0){
            if($p_nSystemId == -1)
              $nUpdate = 1;
          else if($oNonMaster->GetCompletedId() <= $p_nSystemId)
            $nUpdate = 1;
          }
          if($nUpdate == 1){
            if(!$this->Update($p_oNonMaster)){
              $this->m_nError = $this->m_nDBO_SYNC_FAILURE;
              $p_oMaster->SetSynchronising(false);
              $p_oNonMaster->SetSynchronising(false);
              return(false);
            }
          }
          else if(!$oNonMaster->Update($p_oMaster)){
            $this->m_nError = $this->m_nDBO_SYNC_FAILURE;
            $p_oMaster->SetSynchronising(false);
            $p_oNonMaster->SetSynchronising(false);
            return(false);
          }
          $oNonMaster->MoveNext();
          $this->MoveNext();
        }
        else if($nCompare>0){
          if($p_nSystemId == -1){
            if(!$oNonMaster->Delete($p_oNonMaster)){
              $this->m_nError = $this->m_nDBO_SYNC_FAILURE;
              $p_oMaster->SetSynchronising(false);
              $p_oNonMaster->SetSynchronising(false);
              return(false);
            }
          }
          else if($oNonMaster->GetCompletedId() <= $p_nSystemId){
            if(!$oNonMaster->Delete($p_oNonMaster)){
              $this->m_nError = $this->m_nDBO_SYNC_FAILURE;
              $p_oMaster->SetSynchronising(false);
              $p_oNonMaster->SetSynchronising(false);
              return(false);
            }
          }
          else{
            if(!$oNonMaster->Insert($p_oMaster)){
              $this->m_nError = $this->m_nDBO_SYNC_FAILURE;
              $p_oMaster->SetSynchronising(false);
              $p_oNonMaster->SetSynchronising(false);
              return(false);
            }
          }
          $oNonMaster->MoveNext();
        }
        else{
          if($p_nSystemId == -1){
            if(!$this->Insert($p_oNonMaster)){
              $this->m_nError = $this->m_nDBO_SYNC_FAILURE;
              $p_oMaster->SetSynchronising(false);
              $p_oNonMaster->SetSynchronising(false);
              return(false);
            }
          }
          else if($this->m_nCompletedId <= $p_nSystemId){
            if(!$this->Insert($p_oNonMaster)){
              $this->m_nError = $this->m_nDBO_SYNC_FAILURE;
              $p_oMaster->SetSynchronising(false);
              $p_oNonMaster->SetSynchronising(false);
              return(false);
            }
          }
          else{
            if(!$this->Delete($p_oMaster)){
              $this->m_nError = $this->m_nDBO_SYNC_FAILURE;
              $p_oMaster->SetSynchronising(false);
              $p_oNonMaster->SetSynchronising(false);
              return(false);
            }
          }
          $this->MoveNext();
        }
      }
      $p_oMaster->SetSynchronising(false);
      $p_oNonMaster->SetSynchronising(false);
      return(true);
    }
    function Install($p_oSession, $p_nSystemId = 0){
      $this->m_nError = $this->m_nDBO_OK;
      if($p_oSession->GetFileRead()){
        $oFile = @fopen($p_oSession->GetDatabase() . $this->m_sTABLE_NAME . ".dat", "w");
        if($oFile){
          fclose($oFile);
          return(true);
        }
        else{
          $this->m_nError = $this->m_nDBO_CREATE_FAILURE;
          return(false);
        }
      }
      else{
        if(!$this->CheckSession($p_oSession)){
          $this->m_nError = $this->m_nDBO_BAD_SESSION;
          return(false);
        }
        $sSql="CREATE TABLE `tblcompleted` ( `CompletedId` int(10) unsigned NOT NULL auto_increment, `PendingId` int(10) unsigned NOT NULL, `CommandId` int(10) unsigned NOT NULL, `InXML` varchar(2048) NOT NULL, `OutXML` varchar(32768) NOT NULL, `InTimestamp` datetime NOT NULL, `ExecTimestamp` datetime NOT NULL, `Source` int(10) unsigned NOT NULL, `Success` int(10) unsigned NOT NULL, `Mode` varchar(20) NOT NULL, `Data` tinyint(3) unsigned NOT NULL, PRIMARY KEY  (`CompletedId`) ) ENGINE=InnoDB DEFAULT CHARSET=latin1;";
        if(mysql_query($sSql, $p_oSession->GetLink())){
          if($p_nSystemId > 0){
            $this->Clear();
            $this->m_nCompletedId = $p_nSystemId;
            $this->Insert($p_oSession);
            $this->Delete($p_oSession);
          }
          return(true);
        }
        else{
          $this->m_nError = $this->m_nDBO_CREATE_FAILURE;
          $this->m_sErrorInfo = mysql_error() . ":" . $sSql;
          return(false);
        }
      }
    }
    function Match($p_oDBO){
      if($p_oDBO->GetEof())
        return false;
      if(isset($this->m_nCompletedId))
        if($this->m_nCompletedId != $p_oDBO->GetCompletedId())
          return false;
      if(isset($this->m_nPendingId))
        if($this->m_nPendingId != $p_oDBO->GetPendingId())
          return false;
      if(isset($this->m_nCommandId))
        if($this->m_nCommandId != $p_oDBO->GetCommandId())
          return false;
      if(isset($this->m_sInXML))
        if($this->m_sInXML != $p_oDBO->GetInXML())
          return false;
      if(isset($this->m_sOutXML))
        if($this->m_sOutXML != $p_oDBO->GetOutXML())
          return false;
      if(isset($this->m_dInTimestamp))
        if($this->m_dInTimestamp != $p_oDBO->GetInTimestamp())
          return false;
      if(isset($this->m_dExecTimestamp))
        if($this->m_dExecTimestamp != $p_oDBO->GetExecTimestamp())
          return false;
      if(isset($this->m_nSource))
        if($this->m_nSource != $p_oDBO->GetSource())
          return false;
      if(isset($this->m_nSuccess))
        if($this->m_nSuccess != $p_oDBO->GetSuccess())
          return false;
      if(isset($this->m_sMode))
        if($this->m_sMode != $p_oDBO->GetMode())
          return false;
      if(isset($this->m_bData))
        if($this->m_bData != $p_oDBO->GetData())
          return false;
      return true;
    }
    function Upgrade($p_oSession, $p_sVersion){
      $this->m_nError = $this->m_nDBO_OK;
      if($p_oSession->GetFileRead()){
      }
      else{
        if(!$this->CheckSession($p_oSession)){
          $this->m_nError = $this->m_nDBO_BAD_SESSION;
          return(false);
        }
        return(true);
      }
    }
    function GetVersion(){ return("1.00"); }

  }
?>