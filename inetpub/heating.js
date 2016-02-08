  function addAnyArg(argprefix, valprefix, id) {
    var i = id + 1;
    while(document.getElementById(argprefix + i))
      i++;
    var d = document.createElement("div");
    d.setAttribute("id", "div" + argprefix + i);
    document.getElementById(argprefix + "s").appendChild(d);
    var ni = document.createElement("input");
    ni.setAttribute("name", argprefix + i);
    ni.setAttribute("id", argprefix + i);
    ni.setAttribute("type", "text");    
    d.appendChild(ni);
    ni = document.createElement("input");
    ni.setAttribute("name", valprefix + i);
    ni.setAttribute("id", valprefix + i);
    ni.setAttribute("type", "text");    
    d.appendChild(ni);
    ni = document.createElement("input");
    ni.setAttribute("id", "_bp" + argprefix + i);
    ni.setAttribute("type", "button");    
    ni.value = "+";
    d.appendChild(ni);    
    ni.onclick=Function("return addAnyArg('" + argprefix + "','" + valprefix + "'," + i + ");");
    ni = document.createElement("input");
    ni.setAttribute("id", "_bm" + argprefix + i);
    ni.onclick=Function("return removeAnyArg('" + argprefix + "','" + valprefix + "'," + i + ");");
    ni.setAttribute("type", "button");
    ni.value = "-";
    d.appendChild(ni);
    while(i > id + 1) {
      document.getElementById(argprefix + i).value = document.getElementById(argprefix + (i - 1)).value;
      document.getElementById(valprefix + i).value = document.getElementById(valprefix + (i - 1)).value;
      i--;
    }
    document.getElementById(argprefix + (id+1)).value = "";
    document.getElementById(valprefix + (id+1)).value = "";
    return false;
  }

  function addArg(id) {
	  addAnyArg("arg", "val", id);
  }

  function removeAnyArg(argprefix, valprefix, id) {
    var d = document.getElementById(argprefix + "s");
    var i = id + 1;
    while(document.getElementById(argprefix + i)) {
      document.getElementById(argprefix + (i - 1)).value = document.getElementById(argprefix + i).value;
      document.getElementById(valprefix + (i - 1)).value = document.getElementById(valprefix + i).value;
      i++;
    }
    if((id == 1) && (i == 2)) {
      document.getElementById(argprefix + "1").value = "";
      document.getElementById(valprefix + "1").value = "";
    }
    else
      d.removeChild(document.getElementById("div" + argprefix + (i - 1)));
 
    return false;
  }

  function removeArg(id) {
	  removeAnyArg("arg", "val", id);
  }
  

  function addCond(id) {
    var i = id + 1;
    while(document.getElementById("condition" + i))
      i++;
    var d = document.createElement("div");
    d.setAttribute("id", "div" + i);
    document.getElementById("conds").appendChild(d);
    var ni = document.getElementById("condition1").cloneNode(true);
    ni.setAttribute("name", "condition" + i);
    ni.setAttribute("id", "condition" + i);    
    d.appendChild(ni);
    ni = document.createElement("input");
    ni.setAttribute("id", "_bp" + argprefix + i);
    ni.onclick=Function("return addCond(" + i + ")");
    ni.setAttribute("type", "button");    
    ni.value = "+";
    d.appendChild(ni);    
    ni = document.createElement("input");
    ni.setAttribute("id", "_bm" + argprefix + i);
    ni.onclick=Function("return removeCond(" + i + ")");
    ni.setAttribute("type", "button");
    ni.value = "-";
    d.appendChild(ni);    
    while(i > id + 1) {
      document.getElementById("condition" + i).value = document.getElementById("condition" + (i - 1)).value;
      i--;
    }
    document.getElementById("condition" + (id+1)).value = "";
    return false;
  }

  function removeCond(id) {
    var d = document.getElementById("conds");
    var i = id + 1;
    while(document.getElementById("condition" + i)) {
      document.getElementById("condition" + (i - 1)).value = document.getElementById("condition" + i).value;
      i++;
    }
    if((id == 1) && (i == 2))
      document.getElementById("condition1").value = "";
    else
      d.removeChild(document.getElementById("div" + (i - 1)));
 
    return false;
  }
  
  function validateOccupancy()
  {
    for(day = 1; day <= 7; day++)
    {
      for(cell = 0; cell < 3; cell++)
      {
      	myin = document.getElementById("occin" + day + cell);
      	myout = document.getElementById("occout" + day + cell);
        if(parseInt(myin.value) > parseInt(myout.value))
        {
          msg = "In time is greater than Out time for ";
          msg += getDay(day);
		  msg += " for occupancy period " + (cell + 1);
    	  alert(msg);
          return false;
        }
        if(cell < 2)
        {
      	  myin = document.getElementById("occin" + day + (cell + 1));
          if(parseInt(myout.value) > parseInt(myin.value))
          {
            msg = "Out time for occupancy period " + (cell + 1)
            msg += " on " + getDay(day);
            msg += " is greater than In time for";
		    msg += " occupancy period " + (cell + 2);
    	    alert(msg);
            return false;
          }
      	}
      }
    }
    
    return true;
  }
  
  function getDay(Day)
  {
	if(day == 7)
	  return "Sunday";
    else if(day == 1)
      return "Monday";
    else if(day == 2)
      return "Tuesday";
    else if(day == 3)
      return "Wednesday";
    else if(day == 4)
      return "Thursday";
    else if(day == 5)
      return "Friday";
    else if(day == 6)
      return "Saturday";
  }

  function updateElements(eles, display)
  {
      for(var i = 0; i < eles.length; i++)
      {
          eles[i].style.display = display;
      }
  }

  function updateTable()
  {
      var val = document.querySelector('input[name="show"]:checked').value;
      updateElements(document.querySelectorAll('.diva'), 'none');
      updateElements(document.querySelectorAll('.divs'), 'none');
      updateElements(document.querySelectorAll('.divl'), 'none');
      updateElements(document.querySelectorAll('.div' + val), 'block');
  }

  function keyHandler(name) {
      if (document.getElementById(name)) {
          document.getElementById(name).name = name.substr(1);
      }
  }