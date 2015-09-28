<?php

  require_once('dbo.php');

  class CRowtblpending extends CDatabaseObject
  {
    var $m_sTABLE_NAME="tblpending";

    var $m_nPendingId;
    var $m_nGroupId;
    var $m_dInTimestamp;
    var $m_nSource;
    var $m_sInXML;
    var $m_nCompleted;


    function GetPendingId(){ if(isset($this->m_nPendingId)) return($this->m_nPendingId);}
    function SetPendingId($value){$this->m_nPendingId=$value;}
    function ClearPendingId(){ unset($this->m_nPendingId);}
    function GetGroupId(){ if(isset($this->m_nGroupId)) return($this->m_nGroupId);}
    function SetGroupId($value){$this->m_nGroupId=$value;}
    function ClearGroupId(){ unset($this->m_nGroupId);}
    function GetInTimestamp(){ if(isset($this->m_dInTimestamp)) return($this->m_dInTimestamp);}
    function SetInTimestamp($value){$this->m_dInTimestamp=$value;}
    function ClearInTimestamp(){ unset($this->m_dInTimestamp);}
    function GetSource(){ if(isset($this->m_nSource)) return($this->m_nSource);}
    function SetSource($value){$this->m_nSource=$value;}
    function ClearSource(){ unset($this->m_nSource);}
    function GetInXML(){ if(isset($this->m_sInXML)) return($this->m_sInXML);}
    function GetInXMLHTML(){ if(isset($this->m_sInXML)) return(htmlspecialchars($this->m_sInXML));}
    function SetInXML($value){$this->m_sInXML=$value;}
    function ClearInXML(){ unset($this->m_sInXML);}
    function GetCompleted(){ if(isset($this->m_nCompleted)) return($this->m_nCompleted);}
    function SetCompleted($value){$this->m_nCompleted=$value;}
    function ClearCompleted(){ unset($this->m_nCompleted);}


    function Clear(){
      unset($this->m_nPendingId);
      unset($this->m_nGroupId);
      unset($this->m_dInTimestamp);
      unset($this->m_nSource);
      unset($this->m_sInXML);
      unset($this->m_nCompleted);
      $this->m_nFromRecord = 0;
      $this->m_nMaxRecords = -1;
    }
    function Fill(){
      if(isset($this->m_oTable)){
        $oRow = current($this->m_oTable);
        if($oRow != false){
          $this->Clear();
          $this->m_nPendingId = $oRow->GetPendingId();
          $this->m_nGroupId = $oRow->GetGroupId();
          $this->m_dInTimestamp = $oRow->GetInTimestamp();
          $this->m_nSource = $oRow->GetSource();
          $this->m_sInXML = $oRow->GetInXML();
          $this->m_nCompleted = $oRow->GetCompleted();
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
          $this->m_nPendingId = 1;
        else
          $this->m_nPendingId = $sData[0] + 1;
        $sData = array();
        $sData[0] = $this->m_nPendingId;
        $sData[1] = $this->m_nGroupId;
        $sData[2] = $this->m_dInTimestamp;
        $sData[3] = $this->m_nSource;
        $sData[4] = $this->m_sInXML;
        $sData[5] = $this->m_nCompleted;
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
          $sData[0] = $oRow->GetPendingId();
          $sData[1] = $oRow->GetGroupId();
          $sData[2] = $oRow->GetInTimestamp();
          $sData[3] = $oRow->GetSource();
          $sData[4] = $oRow->GetInXML();
          $sData[5] = $oRow->GetCompleted();
          fputs($oFile, $p_oSession->GetSafeRecord($sData, true) . "\r\n");
        }
      }
      fclose($oFile);
      return(true);
    }
    function MatchCriteria($p_sData, $p_bKey){
      if(isset($this->m_nPendingId))
        if($this->m_nPendingId != $p_sData[0])
          return(false);
      if($p_bKey)
        return(true);
      if(isset($this->m_nGroupId))
        if($this->m_nGroupId != $p_sData[1])
          return(false);
      if(isset($this->m_dInTimestamp))
        if($this->m_dInTimestamp != $p_sData[2])
          return(false);
      if(isset($this->m_nSource))
        if($this->m_nSource != $p_sData[3])
          return(false);
      if(isset($this->m_sInXML))
        if($this->m_sInXML != $p_sData[4])
          return(false);
      if(isset($this->m_nCompleted))
        if($this->m_nCompleted != $p_sData[5])
          return(false);
      return(true);
    }
    function GenerateWhereClause($p_oSession, $p_bKeys, &$p_sSql){
      $bKeySet=$p_bKeys;
      $p_sSql="";
      $sConj=" WHERE ";
      if(isset($this->m_nPendingId)){
        if(is_numeric($this->m_nPendingId))
          $p_sSql .= $sConj . "PendingId = " . $this->m_nPendingId;
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
      if(isset($this->m_nGroupId)){
        if(is_numeric($this->m_nGroupId))
          $p_sSql .= $sConj . "GroupId = " . $this->m_nGroupId;
        else
          return(false);
        $sConj = " AND ";
      }
      if(isset($this->m_dInTimestamp)){
        $p_sSql .= $sConj . "InTimestamp = '" . $this->m_dInTimestamp . "'";
        $sConj = " AND ";
      }
      if(isset($this->m_nSource)){
        if(is_numeric($this->m_nSource))
          $p_sSql .= $sConj . "Source = " . $this->m_nSource;
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
      if(isset($this->m_nCompleted)){
        $p_sSql .= $sConj . "Completed = " . $this->GetBool($this->m_nCompleted) . "";
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
              $oObj = new CRowtblpending();
              if($sData[0] != "")
                $oObj->SetPendingId($sData[0]);
              if($sData[1] != "")
                $oObj->SetGroupId($sData[1]);
              if($sData[2] != "")
                $oObj->SetInTimestamp($sData[2]);
              if($sData[3] != "")
                $oObj->SetSource($sData[3]);
              if($sData[4] != "")
                $oObj->SetInXML($sData[4]);
              if($sData[5] != "")
                $oObj->SetCompleted($sData[5]);
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
            $oObj = new CRowtblpending();
            if(!$this->MatchCriteria($sData, true)){
              if($sData[0] != "")
                $oObj->SetPendingId($sData[0]);
              if($sData[1] != "")
                $oObj->SetGroupId($sData[1]);
              if($sData[2] != "")
                $oObj->SetInTimestamp($sData[2]);
              if($sData[3] != "")
                $oObj->SetSource($sData[3]);
              if($sData[4] != "")
                $oObj->SetInXML($sData[4]);
              if($sData[5] != "")
                $oObj->SetCompleted($sData[5]);
            }
            else{
              $bFound = true;
              if(isset($this->m_nPendingId))
                $oObj->SetPendingId($this->GetPendingId());
              else
                $oObj->SetPendingId($sData[0]);
              if(isset($this->m_nGroupId))
                $oObj->SetGroupId($this->GetGroupId());
              else
                $oObj->SetGroupId($sData[1]);
              if(isset($this->m_dInTimestamp))
                $oObj->SetInTimestamp($this->GetInTimestamp());
              else
                $oObj->SetInTimestamp($sData[2]);
              if(isset($this->m_nSource))
                $oObj->SetSource($this->GetSource());
              else
                $oObj->SetSource($sData[3]);
              if(isset($this->m_sInXML))
                $oObj->SetInXML($this->GetInXML());
              else
                $oObj->SetInXML($sData[4]);
              if(isset($this->m_nCompleted))
                $oObj->SetCompleted($this->GetCompleted());
              else
                $oObj->SetCompleted($sData[5]);
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
        if(isset($this->m_nGroupId)){
          if(is_numeric($this->m_nGroupId))
            $sSql .= $sConj . "GroupId = " . $this->m_nGroupId;
          else{
            $this->m_nError = $this->m_nDBO_DATATYPE;
            $this->m_sErrorInfo = "GroupId = " . $this->m_nGroupId;
            return(false);
          }
          $sConj = ",";
        }
        if(isset($this->m_dInTimestamp)){
          $sSql .= $sConj . "InTimestamp = '" . $this->m_dInTimestamp . "'";
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
        if(isset($this->m_sInXML)){
          $sSql .= $sConj . "InXML = '" . $p_oSession->GetSafeSQL($this->m_sInXML) . "'";
          $sConj = ",";
        }
        if(isset($this->m_nCompleted)){
          $sSql .= $sConj . "Completed = " . $this->m_nCompleted;
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
        if(isset($this->m_nGroupId)){
          $sCols .= $sConj . "GroupId";
          if(is_numeric($this->m_nGroupId))
            $sValues .= $sConj . $this->m_nGroupId;
          else{
            $this->m_nError = $this->m_nDBO_DATATYPE;
            $this->m_sErrorInfo = "GroupId = " . $this->m_nGroupId;
            return(false);
          }
          $sConj = ",";
        }
        if(isset($this->m_dInTimestamp)){
          $sCols .= $sConj . "InTimestamp";
          $sValues .= $sConj . "'" . $p_oSession->GetSafeSQL($this->m_dInTimestamp) . "'";
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
        if(isset($this->m_sInXML)){
          $sCols .= $sConj . "InXML";
          $sValues .= $sConj . "'" . $p_oSession->GetSafeSQL($this->m_sInXML) . "'";
          $sConj = ",";
        }
        if(isset($this->m_nCompleted)){
          $sCols .= $sConj . "Completed";
          $sValues .= $sConj . $this->m_nCompleted;
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
        else if(!isset($this->m_nPendingId))
          $this->m_nPendingId= mysql_insert_id();
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
              $oObj = new CRowtblpending();
              if($sData[0] != "")
                $oObj->SetPendingId($sData[0]);
              if($sData[1] != "")
                $oObj->SetGroupId($sData[1]);
              if($sData[2] != "")
                $oObj->SetInTimestamp($sData[2]);
              if($sData[3] != "")
                $oObj->SetSource($sData[3]);
              if($sData[4] != "")
                $oObj->SetInXML($sData[4]);
              if($sData[5] != "")
                $oObj->SetCompleted($sData[5]);
              array_push($this->m_oTable,$oObj);
            }
          }
        }
        if(!$bFirst){
          reset($this->m_oTable);
          $oRow = current($this->m_oTable);
          if($sData[0] != "")
            $this->m_nPendingId = $oRow->GetPendingId();
          if($sData[1] != "")
            $this->m_nGroupId = $oRow->GetGroupId();
          if($sData[2] != "")
            $this->m_dInTimestamp = $oRow->GetInTimestamp();
          if($sData[3] != "")
            $this->m_nSource = $oRow->GetSource();
          if($sData[4] != "")
            $this->m_sInXML = $oRow->GetInXML();
          if($sData[5] != "")
            $this->m_nCompleted = $oRow->GetCompleted();
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
            $oObj = new CRowtblpending();
            if($bFirst){
              $this->m_oTable = array();
              if(!is_null($oRow->PendingId))
                $this->m_nPendingId = $oRow->PendingId;
              if(!is_null($oRow->GroupId))
                $this->m_nGroupId = $oRow->GroupId;
              if(!is_null($oRow->InTimestamp))
                $this->m_dInTimestamp = $oRow->InTimestamp;
              if(!is_null($oRow->Source))
                $this->m_nSource = $oRow->Source;
              if(!is_null($oRow->InXML))
                $this->m_sInXML = $oRow->InXML;
              if(!is_null($oRow->Completed))
                $this->m_nCompleted = $oRow->Completed;
              $bFirst=false;
              $this->m_bBof = false;
              $this->m_bEof = false;
            }
            if(!is_null($oRow->PendingId))
              $oObj->SetPendingId($oRow->PendingId);
            if(!is_null($oRow->GroupId))
              $oObj->SetGroupId($oRow->GroupId);
            if(!is_null($oRow->InTimestamp))
              $oObj->SetInTimestamp($oRow->InTimestamp);
            if(!is_null($oRow->Source))
              $oObj->SetSource($oRow->Source);
            if(!is_null($oRow->InXML))
              $oObj->SetInXML($oRow->InXML);
            if(!is_null($oRow->Completed))
              $oObj->SetCompleted($oRow->Completed);
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
      $sOrder = "PENDINGID";
      $this->SetOrder($sOrder);
      $this->Select($p_oMaster);
      $oNonMaster = new CRowtblpending();
      $oNonMaster->SetOrder($sOrder);
      $oNonMaster->Select($p_oNonMaster);
      while(!($this->GetEof() && $oNonMaster->GetEof())){
        $nCompare = 0;
        if($this->GetEof())
          $nCompare = 1;
        else if($oNonMaster->GetEof())
          $nCompare = -1;
        else if($this->m_nPendingId>$oNonMaster->GetPendingId())
          $nCompare = 1;
        else if($this->m_nPendingId<$oNonMaster->GetPendingId())
          $nCompare = -1;
        if($nCompare==0){
          $nUpdate = 0;
          if($nUpdate != 0){
            if($p_nSystemId == -1)
              $nUpdate = 1;
          else if($oNonMaster->GetPendingId() <= $p_nSystemId)
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
          else if($oNonMaster->GetPendingId() <= $p_nSystemId){
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
          else if($this->m_nPendingId <= $p_nSystemId){
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
        $sSql="CREATE TABLE `tblpending` ( `PendingId` int(10) unsigned NOT NULL auto_increment, `GroupId` int(10) unsigned NOT NULL, `InTimestamp` datetime NOT NULL, `Source` int(10) unsigned NOT NULL, `InXML` varchar(2048) NOT NULL, `Completed` tinyint(3) unsigned NOT NULL, PRIMARY KEY  (`PendingId`) ) ENGINE=InnoDB AUTO_INCREMENT=363 DEFAULT CHARSET=latin1;";
        if(mysql_query($sSql, $p_oSession->GetLink())){
          if($p_nSystemId > 0){
            $this->Clear();
            $this->m_nPendingId = $p_nSystemId;
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
      if(isset($this->m_nPendingId))
        if($this->m_nPendingId != $p_oDBO->GetPendingId())
          return false;
      if(isset($this->m_nGroupId))
        if($this->m_nGroupId != $p_oDBO->GetGroupId())
          return false;
      if(isset($this->m_dInTimestamp))
        if($this->m_dInTimestamp != $p_oDBO->GetInTimestamp())
          return false;
      if(isset($this->m_nSource))
        if($this->m_nSource != $p_oDBO->GetSource())
          return false;
      if(isset($this->m_sInXML))
        if($this->m_sInXML != $p_oDBO->GetInXML())
          return false;
      if(isset($this->m_nCompleted))
        if($this->m_nCompleted != $p_oDBO->GetCompleted())
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