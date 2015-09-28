  function addArg(id) {
    var i = id + 1;
    while(document.getElementById("arg" + i))
      i++;
    var d = document.createElement("div");
    d.setAttribute("id", "div" + i);
    document.getElementById("args").appendChild(d);
    var ni = document.createElement("input");
    ni.setAttribute("name", "arg" + i);
    ni.setAttribute("id", "arg" + i);
    ni.setAttribute("type", "text");    
    d.appendChild(ni);
    ni = document.createElement("input");
    ni.setAttribute("name", "val" + i);
    ni.setAttribute("id", "val" + i);
    ni.setAttribute("type", "text");    
    d.appendChild(ni);
    ni = document.createElement("input");
    ni.setAttribute("id", "_bp" + i);
    ni.setAttribute("type", "button");    
    ni.value = "+";
    d.appendChild(ni);    
    ni.onclick=Function("return addArg(" + i + ");");
    ni = document.createElement("input");
    ni.setAttribute("id", "_bm" + i);
    ni.onclick=Function("return removeArg(" + i + ");");
    ni.setAttribute("type", "button");
    ni.value = "-";
    d.appendChild(ni);    
    while(i > id + 1) {
      document.getElementById("arg" + i).value = document.getElementById("arg" + (i - 1)).value;
      document.getElementById("val" + i).value = document.getElementById("val" + (i - 1)).value;
      i--;
    }
    document.getElementById("arg" + (id+1)).value = "";
    document.getElementById("val" + (id+1)).value = "";
    return false;
  }

  function removeArg(id) {
    var d = document.getElementById("args");
    var i = id + 1;
    while(document.getElementById("arg" + i)) {
      document.getElementById("arg" + (i - 1)).value = document.getElementById("arg" + i).value;
      document.getElementById("val" + (i - 1)).value = document.getElementById("val" + i).value;
      i++;
    }
    if((id == 1) && (i == 2)) {
      document.getElementById("arg1").value = "";
      document.getElementById("val1").value = "";
    }
    else
      d.removeChild(document.getElementById("div" + (i - 1)));
 
    return false;
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
    ni.setAttribute("id", "_bp" + i);
    ni.onclick=Function("return addCond(" + i + ")");
    ni.setAttribute("type", "button");    
    ni.value = "+";
    d.appendChild(ni);    
    ni = document.createElement("input");
    ni.setAttribute("id", "_bm" + i);
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