<?php
  class CDatabaseView
  {
    var $m_oTable;
    var $m_bBof = true;
    var $m_bEof = true;
    var $m_bWildcards = false;
    var $m_sFilter = "";
    var $m_sSortBy = "";
    var $m_nFromRecord = 0;
    var $m_nMaxRecords = -1;
    var $m_nError = 0;
    var $m_sErrorInfo = "";

    var $m_nDBO_RECORD_LENGTH = 1024;

    var $m_nDBO_OK = 0;
    var $m_nDBO_BAD_SESSION = 1;
    var $m_nDBO_BAD_WRITE_FILE = 2;
    var $m_nDBO_BAD_READ_FILE = 3;
    var $m_nDBO_DELETE_FAILURE = 4;
    var $m_nDBO_UPDATE_FAILURE = 5;
    var $m_nDBO_INSERT_FAILURE = 6;
    var $m_nDBO_SELECT_FAILURE = 7;
    var $m_nDBO_CREATE_FAILURE = 8;
    var $m_nDBO_UPGRADE_FAILURE = 9;
    var $m_nDBO_SYNC_FAILURE = 10;
    var $m_nDBO_DATATYPE = 11;
    
    function GetBOF(){ return($this->m_bBof);}
    function GetEOF(){ return($this->m_bEof);}
    function GetSelectFrom(){ return($this->m_nFromRecord);}
    function SetSelectFrom($p_nFrom){ $this->m_nFromRecord = $p_nFrom;}
    function GetMaxRecords(){ return($this->m_nMaxRecords);}
    function SetMaxRecords($p_nMax){ $this->m_nMaxRecords = $p_nMax;}
    function EnableWildcards(){ $this->m_bWildcards = true;}
    function DisableWildcards(){ $this->m_bWildcards = false;}
    function GetWildcards(){ return($this->m_bWildcards);}
    function GetFilter(){ return($this->m_sFilter);}
    function SetFilter($p_sFilter){ $this->m_sFilter = $p_sFilter;}
    function GetOrder(){ return($this->m_sSortBy);}
    function SetOrder($p_sOrder){ $this->m_sSortBy = $p_sOrder;}
    function GetError(){return($this->m_nError);}
    function GetErrorInfo(){return($this->m_sErrorInfo);}
    function GetBool($p_bValue){if($p_bValue) return "1"; else return "0";}
    function GetNow(){return date("Y-m-d H:i:s");}
    function Fill(){}
    function Clear(){}

    function CheckSession($p_oSession){
      if(!isset($p_oSession))
        return(false);
      $oLink = $p_oSession->GetLink();
      if(!isset($oLink))
        return(false);
      else
        return(true);
    }

    function ReadRecord($p_oFile, $p_oSession, &$p_sData){
    
      $bRead = true;
      $sData = "";      
      while($bRead)
      {
        $sData .= fgets($p_oFile, $this->m_nDBO_RECORD_LENGTH);
        if(feof($p_oFile))
          $bRead = false;
        else if(substr($sData, -2) == "\r\n")
          $bRead = false;
        else if(substr($sData, -1) == "\r")
        {
          $sData .= fgets($p_oFile, 1);
          $bRead = false;
        }
      }
      if($sData != "")
        $sData = substr($sData, 0, -2);
      else
        return(false);
      $p_sData = $p_oSession->GetSafeRecord($sData, false);
      return(true);
    }

    function MoveFirst(){
      if(isset($this->m_oTable)){
        if(count($this->m_oTable) > 0){
          if(reset($this->m_oTable) == false){
            $this->m_bBof = true;
            return(false);
          }
          else{
            $this->m_bBof = false;
            $this->m_bEof = false;
            $this->Fill();
            return(true);
          }
        }
        else{
          $this->m_bEof = true;
          $this->m_bBof = true;
          $this->Clear();
          return(false);
        }
      }
      else{
        $this->Clear();
        return(false);
      }
    }

    function MoveLast(){
      if(isset($this->m_oTable)){
        if(count($this->m_oTable) > 0){
          if(end($this->m_oTable) == false){
            $this->m_bEof = true;
            return(false);
          }
          else{
            $this->m_bEof = false;
            $this->m_bBof = false;
            $this->Fill();
            return(true);
          }
        }
        else{
          $this->m_bEof = true;
          $this->m_bBof = true;
          $this->Clear();
          return(false);
        }
      }
      else{
        $this->Clear();
        return(false);
      }
    }

    function MoveNext(){
      if(isset($this->m_oTable)){
        if(count($this->m_oTable) > 0){
          if(next($this->m_oTable) == false){
            $this->m_bEof = true;
            return(false);
          }
          else{
            $this->m_bEof = false;
            $this->Fill();
            return(true);
          }
        }
        else{
          $this->m_bEof = true;
          $this->m_bBof = true;
          $this->Clear();
          return(false);
        }
      }
      else{
        $this->Clear();
        return(false);
      }
    }

    function MovePrevious(){
      if(isset($this->m_oTable)){
        if(count($this->m_oTable) > 0){
          if(prev($this->m_oTable) == false){
            $this->m_bBof = true;
            return(false);
          }
          else{
            $this->m_bBof = false;
            $this->Fill();
            return(true);
          }
        }
        else{
          $this->m_bEof = true;
          $this->m_bBof = true;
          $this->Clear();
          return(false);
        }
      }
      else{
        $this->Clear();
        return(false);
      }
    }

    function GetRecordCount(){
      if(isset($this->m_oTable))
        return(count($this->m_oTable));
      else
        return(0);
    }

    function GetRecordNo(){
      if(isset($this->m_oTable))
        return(key($this->m_oTable));
      else
        return(-1);
    }

    function _Goto($p_nRecordNo){
      if(isset($this->m_oTable)){
        reset($this->m_oTable);
        do{
          if(key($this->m_oTable) == $p_nRecordNo){
            $this->Fill();
            return(true);
          }
        }while(next($this->m_oTable));
        return(false);
      }
      else
        return(false);
    }

    function Select($p_oSession){}
    function Find($p_oDBO)
    {
      $this->MoveFirst();
      if($this->GetEof())
        return false;
      else
      {
        if($p_oDBO->Match($this))
          return true;
        else
          return $this->FindNext($p_oDBO);
      }
    }
    function FindNext($p_oDBO)
    {
      $this->MoveNext();
      while(!$p_oDBO->Match($this) && !$this->GetEof())
        $this->MoveNext();
      return(!$this->GetEof());
    }
    function Match($p_oDBO){return(false);}
  }
?>