<?php

   class CDatabaseSession {

    var $m_sHost = "localhost";
    var $m_nPort = 3306;
    var $m_sLogin = "root";
    var $m_sDatabase;
    var $m_oLink;
    var $m_bUser;
    var $m_nTransactionLevel = 0;
    var $m_bFileRead = false;
    var $m_sPrivateKey = "Default Private Key";
    var $m_bSynchronising = false;

    function GetError()
    {
      if($this->m_oLink)
        return mysql_error($this->m_oLink);
      else
        return "";
    }

    function GetSynchronising(){ return($this->m_bSynchronising); }
    function SetSynchronising($p_bSync){ $this->m_bSynchronising = $p_bSync; }

    function GetDatabaseAlias()
    {
      if(isset($this->m_sDatabase))
      {
        if($this->m_bFileRead)
          return basename($this->m_sDatabase);
        else
          return $this->m_sDatabase;
      }
      else
        return "";
    }
    function GetDatabase(){if(isset($this->m_sDatabase)) return $this->m_sDatabase; }
    function GetLink(){if(isset($this->m_oLink)) return $this->m_oLink; }
    function GetFileRead(){return($this->m_bFileRead);}
    function GetPrivateKey(){return($this->m_sPrivateKey);}
    function SetPrivateKey($p_sPrivateKey){$this->m_sPrivateKey = $p_sPrivateKey;}

    function SelectDatabase($p_sDatabase)
    {
      $this->m_bFileRead = false;
      if($this->m_oLink && strlen($p_sDatabase)){
        if(is_dir($p_sDatabase))
        {
          $this->m_oLink = 0;
          $this->m_bFileRead = true;
          $this->m_sDatabase = $p_sDatabase;
          return true;
        }
        else if(mysql_selectdb($p_sDatabase))
        {
          $this->m_sDatabase = $p_sDatabase;
          return true;
        }
        else
        {
          $this->m_oLink = 0;
          $this->m_sDatabase = "";
          return false;
        }
      }
      else if(is_dir($p_sDatabase))
      {
        $this->m_oLink = 0;
        $this->m_bFileRead = true;
        $this->m_sDatabase = $p_sDatabase;
        return true;
      }
      else
      {
        $this->m_oLink = 0;
        $this->m_sDatabase = "";
        return false;
      }
    }

    function GetSafeSQL($p_sString)
    {
      $sData = mysql_real_escape_string($p_sString, $this->m_oLink);
      return($sData);
    }

    function GetSafeRecord($p_sData, $p_bWrite)
    {
      if($p_bWrite)
      {
        for($nLoop = 0; $nLoop < count($p_sData); $nLoop++)
          $p_sData[$nLoop] = str_replace("|", "\bar", $p_sData[$nLoop]);
        $sData = implode("|", $p_sData);
        $sData = str_replace("\n", "\\n", $sData);
        return($sData);
      }
      else
      {
        $sData = str_replace("\\n", "\n", $p_sData);
        $sData = explode("|", $sData);
        for($nLoop = 0; $nLoop < count($sData); $nLoop++)
          $sData[$nLoop] = str_replace("\bar", "|", $sData[$nLoop]);
        return($sData);
      }
    }

    function GetUser(){ return($this->m_sLogin); }
    function Login($p_sHost, $p_sLogin, $p_sPassword, $p_nPort = 3306)
    {
      if($this->m_oLink = mysql_pconnect($p_sHost . ":" . $p_nPort, $p_sLogin, $p_sPassword))
      {
        $this->m_sHost = $p_sHost;
        $this->m_sLogin = $p_sLogin;
        $this->m_nPort = $p_nPort;
        return true;
      }
      else
      {
        $this->m_sHost = "";
        $this->m_sLogin = "";
        $this->m_nPort = 3306;
        return false;
      }
    }
    
    function CreateDatabase($p_sDatabase)
    {
      if($this->m_oLink)
      {
        $sSql = "CREATE DATABASE " . $p_sDatabase;
        if(mysql_query($sSql,$this->m_oLink))
          return $this->SelectDatabase($p_sDatabase);
        else
          return false;
      }
      else
        mkdir($p_sDatabase);
    }

    function CreateUser($p_sUserName, $p_sPassword)
    {
      if($this->m_oLink)
      {
        $sSql = "GRANT ALL PRIVILEGES ON " . $this->m_sDatabase . ".* TO '" . $this->GetSafeSQL($p_sUserName) . "'@'" . $this->m_sHost . "' IDENTIFIED BY '" . $this->GetSafeSQL($p_sPassword) . "'";
        return mysql_query($sSql, $this->m_oLink);
      }
      else
        return false;
    }

    function ConnectDatabase($p_sDatabase, $p_sHost = "", $p_sLogin = "", $p_sPassword = "", $p_nPort = 3306)
    {
      if(strlen($p_sHost))
      {
        if($this->Login($p_sHost, $p_sLogin, $p_sPassword, $p_nPort))
          return $this->SelectDatabase($p_sDatabase);
        else
          return false;
      }
      else if(is_dir($p_sDatabase))
        return $this->SelectDatabase($p_sDatabase);
      else
        return false;
    }


    function BeginTrans()
    {
      if($this->m_nTransactionLevel > 0)
      {
        $this->m_nTransactionLevel++;
        return true;
      }
      else
      {
        $this->m_nTransactionLevel++;
        if($this->m_bFileRead)
          return(true);
        else
          return mysql_query("BEGIN", $this->m_oLink);
      }
    }
    function CommitTrans()
    {
      if($this->m_nTransactionLevel == 1)
      {
        $this->m_nTransactionLevel--;
        if($this->m_bFileRead)
          return(true);
        else
          return mysql_query("COMMIT", $this->m_oLink);
      }
      else if($this->m_nTransactionLevel == 0)
        return false;
      else
      {
        $this->m_nTransactionLevel--;
        return true;
      }
    }
    function RollbackTrans()
    {
      if($this->m_nTransactionLevel > 0)
      {
        $this->m_nTransactionLevel = 0;
        if($this->m_bFileRead)
          return(true);
        else
          return mysql_query("ROLLBACK", $this->m_oLink);
      }
      else
        return false;
    }

  }
?>