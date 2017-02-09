package Development

import Plans.Generic.Allocation.{LockCurrency, LockUnits}
import Plans.Plan
import Startup.With
import bwapi.{Color, Position, UnitCommandType}
import bwta.BWTA

import scala.collection.JavaConverters._

object Overlay {
  
  var enabled:Boolean = true
  
  def onFrame() {
    if (!enabled) {
      return
    }
    With.game.setTextSize(bwapi.Text.Size.Enum.Small)
    _drawTerrain()
    _drawUnits()
    With.game.drawTextScreen(5, 5, _describePlanTree(With.gameplan, 0, 0))
    _drawPlans(With.gameplan)
    _drawResources()
  }
  
  def _drawTextLabel(textLines:Iterable[String], unit:bwapi.Unit) {
    val horizontalMargin = 2
    val width = (9 * textLines.map(_.size).max) / 2 + 2 * horizontalMargin
    val height = 11 * textLines.size
    val x = unit.getPosition.getX - width/2 - horizontalMargin
    val y = unit.getPosition.getY - height/2
  
    With.game.drawBox(bwapi.CoordinateType.Enum.Map, x, y, x+width, y+height, bwapi.Color.Grey, true)
    With.game.drawTextMap(unit.getX-width/2, unit.getY-height/2, textLines.mkString("\n"))
  }
  
  def _drawUnits() {
    With.ourUnits
      .filter(unit => Debugger.highlitUnits.contains(unit))
      .foreach(unit =>
      With.game.drawCircleMap(unit.getPosition, 32, bwapi.Color.Orange))
    With.ourUnits
      .filterNot(_.getLastCommand.getUnitCommandType == UnitCommandType.None)
      .foreach(unit => _drawTextLabel(List(
        //This gives us the uninteresting name of the Lock plan
        //With.recruiter.getAssignment(unit).map(_getPlanNameOrDescription(_)).getOrElse(""),
        //unit.toString.replace("Unit@", ""),
        unit.getLastCommand.getUnitCommandType.toString), unit))
  }
  
  def _describePlanTree(plan:Plan, childOrder:Integer, depth:Integer):String = {
    if (_isRelevant(plan)) {
      (_describePlan(plan, childOrder, depth)
        ++ plan.getChildren.zipWithIndex.map(x => _describePlanTree(x._1, x._2, depth + 1)))
        .mkString("")
    } else {
      ""
    }
  }
  
  def _getPlanNameOrDescription(plan:Plan):String = {
    plan.description.get.getOrElse(plan.getClass.getSimpleName.replace("Plan", "").replace("$anon$", "Plan"))
  }
  
  def _describePlan(plan:Plan, childOrder:Integer, depth:Integer):String = {
    val planName = _getPlanNameOrDescription(plan)
    val checkbox = if (plan.isComplete) "[X] " else "[_] "
    
    val spacer = "  " * depth
    val leftColumn =
    (checkbox
      ++ spacer
      ++ "#"
      ++ (childOrder + 1).toString
      ++ " "
      ++ planName)
    
    leftColumn + " " * Math.max(0, 45 - leftColumn.length) + "\n"
  }
  
  def _isRelevant(plan:Plan):Boolean = {
    if (plan.isComplete) {
      return plan.isInstanceOf[LockCurrency] || plan.isInstanceOf[LockUnits]
    }
    
    plan.getChildren.exists(_isRelevant(_))
  }
  
  def _drawTerrain() {
    var i = 0
    With.map.ourMiningAreas.foreach(area => With.game.drawBoxMap(
      area.start.toPosition,
      area.end.toPosition,
      Color.Red))
    BWTA.getBaseLocations.asScala.foreach(base => {
      val label = (if (base.isStartLocation) "Start location" else "Expansion") +
        " #" + i
        "\n" +
        (if (With.game.isExplored(base.getTilePosition)) "Explored" else "Unexplored")
      With.game.drawCircleMap(base.getPosition, 80, Color.Blue)
      With.game.drawTextMap(base.getPosition, label)
      i += 1
    })
    BWTA.getRegions.asScala .foreach(region => {
  
        _drawPolygonPositions(region.getPolygon.getPoints.asScala)
        
        With.game.drawLineMap(
          region.getPolygon.getPoints.asScala.head,
          region.getPolygon.getPoints.asScala.last,
          bwapi.Color.Brown)
        
        With.game.drawTextMap(
          region.getCenter,
          region.getCenter.toString ++
          "\n" ++
          region.getCenter.toTilePosition.toString)
        
        region.getChokepoints.asScala.foreach(
          choke => {
            With.game.drawLineMap(choke.getSides.first, choke.getSides.second, bwapi.Color.Purple)
            With.game.drawTextMap(
              choke.getCenter,
              choke.getCenter.toString ++
              "\n" ++
              choke.getCenter.toTilePosition.toString)
          })
      }
    )
  }
  
  def _drawPolygonPositions(points:Iterable[Position], color:bwapi.Color = bwapi.Color.Brown) {
    points.reduce((p1, p2) => { With.game.drawLineMap(p1, p2, color); p2 })
    With.game.drawLineMap(points.head, points.last, color)
  }
  
  def _drawPlans(plan:Plan) {
    plan.drawOverlay()
    plan.getChildren.foreach(_drawPlans)
  }
  
  def _drawResources() {
    With.game.drawTextScreen(
      305,
      5,
      With.bank.getPrioritizedRequests
        .map(r =>
          (if (r.isSatisfied) "X " else "  ") ++
          (if (r.minerals > 0)  r.minerals  .toString ++ "m " else "") ++
          (if (r.gas > 0)       r.gas       .toString ++ "g " else "") ++
          (if (r.supply > 0)    r.supply    .toString ++ "s " else ""))
        .mkString("\n"))
  }
}
