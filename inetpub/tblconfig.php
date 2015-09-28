<?php

  require_once('dbo.php');

  class CRowtblconfig extends CDatabaseObject
  {
    var $m_sTABLE_NAME="tblconfig";

    var $m_nConfigId;
    var $m_sParam;
    var $m_sValue;
    var $m_sDescription;
    var $m_bModifiable;


    function GetConfigId(){ if(isset($this->m_nConfigId)) return($this->m_nConfigId);}
    function SetConfigId($value){$this->m_nConfigId=$value;}
    function ClearConfigId(){ unset($this->m_nConfigId);}
    function GetParam(){ if(isset($this->m_sParam)) return($this->m_sParam);}
    function GetParamHTML(){ if(isset($this->m_sParam)) return(htmlspecialchars($this->m_sParam));}
    function SetParam($value){$this->m_sParam=$value;}
    function ClearParam(){ unset($this->m_sParam);}
    function GetValue(){ if(isset($this->m_sValue)) return($this->m_sValue);}
    function GetValueHTML(){ if(isset($this->m_sValue)) return(htmlspecialchars($this->m_sValue));}
    function SetValue($value){$this->m_sValue=$value;}
    function ClearValue(){ unset($this->m_sValue);}
    function GetDescription(){ if(isset($this->m_sDescription)) return($this->m_sDescription);}
    function GetDescriptionHTML(){ if(isset($this->m_sDescription)) return(htmlspecialchars($this->m_sDescription));}
    function SetDescription($value){$this->m_sDescription=$value;}
    function ClearDescription(){ unset($this->m_sDescription);}
    function GetModifiable(){ if(isset($this->m_bModifiable)) return($this->m_bModifiable);}
    function SetModifiable($value){$this->m_bModifiable=$value;}
    function ClearModifiable(){ unset($this->m_bModifiable);}


    function Clear(){
      unset($this->m_nConfigId);
      unset($this->m_sParam);
      unset($this->m_sValue);
      unset($this->m_sDescription);
      unset($this->m_bModifiable);
      $this->m_nFromRecord = 0;
      $this->m_nMaxRecords = -1;
    }
    function Fill(){
      if(isset($this->m_oTable)){
        $oRow = current($this->m_oTable);
        if($oRow != false){
          $this->Clear();
          $this->m_nConfigId = $oRow->GetConfigId();
          $this->m_sParam = $oRow->GetParam();
          $this->m_sValue = $oRow->GetValue();
          $this->m_sDescription = $oRow->GetDescription();
          $this->m_bModifiable = $oRow->GetModifiable();
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
          $this->m_nConfigId = 1;
        else
          $this->m_nConfigId = $sData[0] + 1;
        $sData = array();
        $sData[0] = $this->m_nConfigId;
        $sData[1] = $this->m_sParam;
        $sData[2] = $this->m_sValue;
        $sData[3] = $this->m_sDescription;
        $sData[4] = $this->m_bModifiable;
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
          $sData[0] = $oRow->GetConfigId();
          $sData[1] = $oRow->GetParam();
          $sData[2] = $oRow->GetValue();
          $sData[3] = $oRow->GetDescription();
          $sData[4] = $oRow->GetModifiable();
          fputs($oFile, $p_oSession->GetSafeRecord($sData, true) . "\r\n");
        }
      }
      fclose($oFile);
      return(true);
    }
    function MatchCriteria($p_sData, $p_bKey){
      if(isset($this->m_nConfigId))
        if($this->m_nConfigId != $p_sData[0])
          return(false);
      if($p_bKey)
        return(true);
      if(isset($this->m_sParam))
        if($this->m_sParam != $p_sData[1])
          return(false);
      if(isset($this->m_sValue))
        if($this->m_sValue != $p_sData[2])
          return(false);
      if(isset($this->m_sDescription))
        if($this->m_sDescription != $p_sData[3])
          return(false);
      if(isset($this->m_bModifiable))
        if($this->m_bModifiable != $p_sData[4])
          return(false);
      return(true);
    }
    function GenerateWhereClause($p_oSession, $p_bKeys, &$p_sSql){
      $bKeySet=$p_bKeys;
      $p_sSql="";
      $sConj=" WHERE ";
      if(isset($this->m_nConfigId)){
        if(is_numeric($this->m_nConfigId))
          $p_sSql .= $sConj . "ConfigId = " . $this->m_nConfigId;
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
      if(isset($this->m_sParam)){
        if($this->m_bWildcards){
          if(strcspn($this->m_sParam, "_%") == strlen($this->m_sParam))
            $p_sSql .= $sConj . "Param = '" . $p_oSession->GetSafeSQL($this->m_sParam) . "'";
          else
            $p_sSql .= $sConj . "Param LIKE '" . $p_oSession->GetSafeSQL($this->m_sParam) . "'";
        }
        else
          $p_sSql .= $sConj . "Param = '" . $p_oSession->GetSafeSQL($this->m_sParam) . "'";
        $sConj = " AND ";
      }
      if(isset($this->m_sValue)){
        if($this->m_bWildcards){
          if(strcspn($this->m_sValue, "_%") == strlen($this->m_sValue))
            $p_sSql .= $sConj . "Value = '" . $p_oSession->GetSafeSQL($this->m_sValue) . "'";
          else
            $p_sSql .= $sConj . "Value LIKE '" . $p_oSession->GetSafeSQL($this->m_sValue) . "'";
        }
        else
          $p_sSql .= $sConj . "Value = '" . $p_oSession->GetSafeSQL($this->m_sValue) . "'";
        $sConj = " AND ";
      }
      if(isset($this->m_sDescription)){
        if($this->m_bWildcards){
          if(strcspn($this->m_sDescription, "_%") == strlen($this->m_sDescription))
            $p_sSql .= $sConj . "Description = '" . $p_oSession->GetSafeSQL($this->m_sDescription) . "'";
          else
            $p_sSql .= $sConj . "Description LIKE '" . $p_oSession->GetSafeSQL($this->m_sDescription) . "'";
        }
        else
          $p_sSql .= $sConj . "Description = '" . $p_oSession->GetSafeSQL($this->m_sDescription) . "'";
        $sConj = " AND ";
      }
      if(isset($this->m_bModifiable)){
        $p_sSql .= $sConj . "Modifiable = " . $this->GetBool($this->m_bModifiable) . "";
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
              $oObj = new CRowtblconfig();
              if($sData[0] != "")
                $oObj->SetConfigId($sData[0]);
              if($sData[1] != "")
                $oObj->SetParam($sData[1]);
              if($sData[2] != "")
                $oObj->SetValue($sData[2]);
              if($sData[3] != "")
                $oObj->SetDescription($sData[3]);
              if($sData[4] != "")
                $oObj->SetModifiable($sData[4]);
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
            $oObj = new CRowtblconfig();
            if(!$this->MatchCriteria($sData, true)){
              if($sData[0] != "")
                $oObj->SetConfigId($sData[0]);
              if($sData[1] != "")
                $oObj->SetParam($sData[1]);
              if($sData[2] != "")
                $oObj->SetValue($sData[2]);
              if($sData[3] != "")
                $oObj->SetDescription($sData[3]);
              if($sData[4] != "")
                $oObj->SetModifiable($sData[4]);
            }
            else{
              $bFound = true;
              if(isset($this->m_nConfigId))
                $oObj->SetConfigId($this->GetConfigId());
              else
                $oObj->SetConfigId($sData[0]);
              if(isset($this->m_sParam))
                $oObj->SetParam($this->GetParam());
              else
                $oObj->SetParam($sData[1]);
              if(isset($this->m_sValue))
                $oObj->SetValue($this->GetValue());
              else
                $oObj->SetValue($sData[2]);
              if(isset($this->m_sDescription))
                $oObj->SetDescription($this->GetDescription());
              else
                $oObj->SetDescription($sData[3]);
              if(isset($this->m_bModifiable))
                $oObj->SetModifiable($this->GetModifiable());
              else
                $oObj->SetModifiable($sData[4]);
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
        if(isset($this->m_sParam)){
          $sSql .= $sConj . "Param = '" . $p_oSession->GetSafeSQL($this->m_sParam) . "'";
          $sConj = ",";
        }
        if(isset($this->m_sValue)){
          $sSql .= $sConj . "Value = '" . $p_oSession->GetSafeSQL($this->m_sValue) . "'";
          $sConj = ",";
        }
        if(isset($this->m_sDescription)){
          $sSql .= $sConj . "Description = '" . $p_oSession->GetSafeSQL($this->m_sDescription) . "'";
          $sConj = ",";
        }
        if(isset($this->m_bModifiable)){
          $sSql .= $sConj . "Modifiable = " . $this->GetBool($this->m_bModifiable);
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
        if(isset($this->m_nConfigId)){
          $sCols .= $sConj . "ConfigId";
          if(is_numeric($this->m_nConfigId))
            $sValues .= $sConj . $this->m_nConfigId;
          else{
            $this->m_nError = $this->m_nDBO_DATATYPE;
            $this->m_sErrorInfo = "ConfigId = " . $this->m_nConfigId;
            return(false);
          }
          $sConj = ",";
        }
        if(isset($this->m_sParam)){
          $sCols .= $sConj . "Param";
          $sValues .= $sConj . "'" . $p_oSession->GetSafeSQL($this->m_sParam) . "'";
          $sConj = ",";
        }
        if(isset($this->m_sValue)){
          $sCols .= $sConj . "Value";
          $sValues .= $sConj . "'" . $p_oSession->GetSafeSQL($this->m_sValue) . "'";
          $sConj = ",";
        }
        if(isset($this->m_sDescription)){
          $sCols .= $sConj . "Description";
          $sValues .= $sConj . "'" . $p_oSession->GetSafeSQL($this->m_sDescription) . "'";
          $sConj = ",";
        }
        if(isset($this->m_bModifiable)){
          $sCols .= $sConj . "Modifiable";
          $sValues .= $sConj . $this->GetBool($this->m_bModifiable);
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
        else if(!isset($this->m_nConfigId))
          $this->m_nConfigId= mysql_insert_id();
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
              $oObj = new CRowtblconfig();
              if($sData[0] != "")
                $oObj->SetConfigId($sData[0]);
              if($sData[1] != "")
                $oObj->SetParam($sData[1]);
              if($sData[2] != "")
                $oObj->SetValue($sData[2]);
              if($sData[3] != "")
                $oObj->SetDescription($sData[3]);
              if($sData[4] != "")
                $oObj->SetModifiable($sData[4]);
              array_push($this->m_oTable,$oObj);
            }
          }
        }
        if(!$bFirst){
          reset($this->m_oTable);
          $oRow = current($this->m_oTable);
          if($sData[0] != "")
            $this->m_nConfigId = $oRow->GetConfigId();
          if($sData[1] != "")
            $this->m_sParam = $oRow->GetParam();
          if($sData[2] != "")
            $this->m_sValue = $oRow->GetValue();
          if($sData[3] != "")
            $this->m_sDescription = $oRow->GetDescription();
          if($sData[4] != "")
            $this->m_bModifiable = $oRow->GetModifiable();
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
            $oObj = new CRowtblconfig();
            if($bFirst){
              $this->m_oTable = array();
              if(!is_null($oRow->ConfigId))
                $this->m_nConfigId = $oRow->ConfigId;
              if(!is_null($oRow->Param))
                $this->m_sParam = $oRow->Param;
              if(!is_null($oRow->Value))
                $this->m_sValue = $oRow->Value;
              if(!is_null($oRow->Description))
                $this->m_sDescription = $oRow->Description;
              if(!is_null($oRow->Modifiable))
                $this->m_bModifiable = $oRow->Modifiable;
              $bFirst=false;
              $this->m_bBof = false;
              $this->m_bEof = false;
            }
            if(!is_null($oRow->ConfigId))
              $oObj->SetConfigId($oRow->ConfigId);
            if(!is_null($oRow->Param))
              $oObj->SetParam($oRow->Param);
            if(!is_null($oRow->Value))
              $oObj->SetValue($oRow->Value);
            if(!is_null($oRow->Description))
              $oObj->SetDescription($oRow->Description);
            if(!is_null($oRow->Modifiable))
              $oObj->SetModifiable($oRow->Modifiable);
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
      $sOrder = "CONFIGID";
      $this->SetOrder($sOrder);
      $this->Select($p_oMaster);
      $oNonMaster = new CRowtblconfig();
      $oNonMaster->SetOrder($sOrder);
      $oNonMaster->Select($p_oNonMaster);
      while(!($this->GetEof() && $oNonMaster->GetEof())){
        $nCompare = 0;
        if($this->GetEof())
          $nCompare = 1;
        else if($oNonMaster->GetEof())
          $nCompare = -1;
        else if($this->m_nConfigId>$oNonMaster->GetConfigId())
          $nCompare = 1;
        else if($this->m_nConfigId<$oNonMaster->GetConfigId())
          $nCompare = -1;
        if($nCompare==0){
          $nUpdate = 0;
          if($nUpdate != 0){
            if($p_nSystemId == -1)
              $nUpdate = 1;
          else if($oNonMaster->GetConfigId() <= $p_nSystemId)
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
          else if($oNonMaster->GetConfigId() <= $p_nSystemId){
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
          else if($this->m_nConfigId <= $p_nSystemId){
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
        $sSql="CREATE TABLE `tblconfig` ( `ConfigId` int(10) unsigned NOT NULL auto_increment, `Param` varchar(15) NOT NULL, `Value` varchar(1000) NOT NULL, `Description` varchar(100) NOT NULL, `Modifiable` tinyint(3) unsigned NOT NULL, PRIMARY KEY  (`ConfigId`), KEY `ParamKey` (`Param`) ) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=latin1;";
        if(mysql_query($sSql, $p_oSession->GetLink())){
          if($p_nSystemId > 0){
            $this->Clear();
            $this->m_nConfigId = $p_nSystemId;
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
      if(isset($this->m_nConfigId))
        if($this->m_nConfigId != $p_oDBO->GetConfigId())
          return false;
      if(isset($this->m_sParam))
        if($this->m_sParam != $p_oDBO->GetParam())
          return false;
      if(isset($this->m_sValue))
        if($this->m_sValue != $p_oDBO->GetValue())
          return false;
      if(isset($this->m_sDescription))
        if($this->m_sDescription != $p_oDBO->GetDescription())
          return false;
      if(isset($this->m_bModifiable))
        if($this->m_bModifiable != $p_oDBO->GetModifiable())
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