package Global.Information

import Types.UnitInfo.UnitInfo

class Units {
  
  def id(id:Int):UnitInfo = {
    throw new Exception
  }
  
  def all:Iterable[UnitInfo] = {
    throw new Exception
  }
  
  def ours:Iterable[UnitInfo] = {
    throw new Exception
  }
  
  def enemy:Iterable[UnitInfo] = {
    throw new Exception
  }
  
  def ally:Iterable[UnitInfo] = {
    throw new Exception
  }
  
  def onFrame() {
    //Map un-mapped units
  }
  
  def onUnitDestroy(unit: bwapi.Unit) {
    
  }
}
