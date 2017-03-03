package Development

import Geometry.Grids.Abstract.Grid
import Geometry.TileRectangle
import Global.Combat.Battle.Battle
import Plans.Allocation.{LockCurrency, LockUnits}
import Plans.Plan
import Startup.With
import Types.UnitInfo.ForeignUnitInfo
import Utilities.Enrichment.EnrichPosition._
import bwapi.{Color, Position, UnitCommandType}
import bwta.BWTA

import scala.collection.JavaConverters._

object Overlay {
  def onFrame() {
    if (With.configuration.enableOverlay) {
      With.game.setTextSize(bwapi.Text.Size.Enum.Small)
      if (With.configuration.enableOverlayBasePlacement)  _drawBases()
      if (With.configuration.enableOverlayBattles)        _drawBattles()
      if (With.configuration.enableOverlayEconomy)        _drawEconomy()
      if (With.configuration.enableOverlayExclusions)     _drawExclusions()
      if (With.configuration.enableOverlayGrids)          _drawGrids()
      if (With.configuration.enableOverlayUnits)          _drawUnits()
      if (With.configuration.enableOverlayPlans)          _drawPlans()
      if (With.configuration.enableOverlayResources)      _drawResources()
      if (With.configuration.enableOverlayTerrain)        _drawTerrain()
      if (With.configuration.enableOverlayTrackedUnits)   _drawTrackedUnits()
      if (With.configuration.enableOverlayWorkers)        _drawWorkers()
    }
  }
  
  def _drawBases() {
    With.geography.ourHarvestingAreas.foreach(area => _drawTileRectangle(area, Color.Red))
    With.geography.basePositions.foreach(position => _drawTileRectangle(new TileRectangle(position, position.add(4, 3)), Color.Yellow))
  }
  
  def _drawBattles() {
    With.battles.all.foreach(_drawBattle)
  }
  
  def _drawBattle(battle:Battle) {
    //if (battle.enemy.strength * battle.us.strength == 0) return
    //if (battle.us.vanguard.getDistance(battle.enemy.vanguard) > 32 * 20) return
    With.game.drawCircleMap(battle.focus, 8, Color.Brown)
    With.game.drawCircleMap(battle.us.vanguard, 8, Color.Green)
    With.game.drawCircleMap(battle.enemy.vanguard, 8, Color.Red)
    With.game.drawLineMap(battle.focus, battle.us.vanguard, Color.Green)
    With.game.drawLineMap(battle.focus, battle.enemy.vanguard, Color.Red)
    With.game.drawBoxMap(
      battle.us.units.map(_.position).minBound,
      battle.us.units.map(_.position).maxBound,
      Color.Green)
    With.game.drawBoxMap(
      battle.enemy.units.map(_.position).minBound,
      battle.enemy.units.map(_.position).maxBound,
      Color.Red)
    _drawTextLabel(
      List(battle.us.strength/100 + " - " + battle.enemy.strength/100),
      battle.focus,
      drawBackground = true,
      backgroundColor = Color.Brown)
  }
  
  def _drawEconomy() {
    val labels = List(
      "Active miners:",
      "Active drillers:",
      "Minerals per minute:",
      "Gas per minute:",
      "Total minerals (est.):",
      "Total gas (est.):",
      "Total minerals (real):",
      "Total gas (real):"
    )
    val values = List(
      With.economy.ourActiveMiners.size,
      With.economy.ourActiveDrillers.size,
      With.economy.ourMineralIncomePerMinute,
      With.economy.ourGasIncomePerMinute,
      With.economy.ourEstimatedTotalMinerals.toInt,
      With.economy.ourEstimatedTotalGas.toInt,
      With.economy.ourActualTotalMinerals,
      With.economy.ourActualTotalGas
    )
    With.game.drawTextScreen(450, 5, labels.mkString("\n"))
    With.game.drawTextScreen(550, 5, values.mkString("\n"))
  }
  
  def _drawExclusions() {
    With.geography._gasExclusionCache.get.foreach(box => _drawTileRectangle(box, Color.Teal))
    With.geography._mineralExclusionCache.get.foreach(box => _drawTileRectangle(box, Color.Teal))
    With.geography._resourceClusterCache.get.foreach(cluster => {
      val centroid = cluster.centroid
      cluster.foreach(position =>
        With.game.drawLineMap(
          centroid.toPosition,
          position.toPosition,
          Color.Teal))})
  }
  
  def _drawGrids() {
    _drawGrid(With.grids.friendlyGroundStrength, 0, 1)
    _drawGrid(With.grids.enemyGroundStrength, 0, 0)
  }
  
  def _drawGrid[T](map:Grid[T], offsetX:Int=0, offsetY:Int=0) {
    map.positions
      .filter(tilePosition => map.get(tilePosition) != 0)
      .foreach(tilePosition => With.game.drawTextMap(tilePosition.toPosition.add(offsetX*16, offsetY*13), map.repr(map.get(tilePosition))))
  }
  
  def _drawPlans() {
    With.game.drawTextScreen(5, 5, _describePlanTree(With.gameplan, 0, 0))
    _drawPlansRecursively(With.gameplan)
  }
  
  def _drawPlansRecursively(plan:Plan) {
    plan.drawOverlay()
    plan.getChildren.foreach(_drawPlansRecursively)
  }
  
  def _drawUnits() {
    With.units.ours
      .filter(unit => Debugger.highlitUnits.contains(unit))
      .foreach(unit =>
      With.game.drawCircleMap(unit.position, 32, bwapi.Color.Orange))
    With.units.ours
      .filterNot(_.command.getUnitCommandType == UnitCommandType.None)
      .foreach(unit => _drawTextLabel(
        List(
          unit.command.getUnitCommandType.toString,
          With.commander._lastCommands.get(unit).getOrElse("")),
        unit.position,
        drawBackground = false))
  }
  
  def _drawTerrain() {
    BWTA.getRegions.asScala.foreach(region => {
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
  
  def _drawResources() {
    With.game.drawTextScreen(
      305,
      5,
      
      With.bank.getPrioritizedRequests
        .take(8)
        .map(r =>
          (if (r.isSatisfied) "X " else "  ") ++
          (if (r.minerals > 0)  r.minerals  .toString ++ "m " else "") ++
          (if (r.gas > 0)       r.gas       .toString ++ "g " else "") ++
          (if (r.supply > 0)    r.supply    .toString ++ "s " else ""))
        .mkString("\n"))
  }
  
  def _drawTrackedUnits() {
    With.units.enemy.foreach(_drawTrackedUnit)
    With.units.neutral.foreach(_drawTrackedUnit)
  }
  
  def _drawTrackedUnit(trackedUnit:ForeignUnitInfo) {
    if (trackedUnit._possiblyStillThere && ! trackedUnit.visible) {
      With.game.drawCircleMap(
        trackedUnit.position,
        trackedUnit.utype.width / 2,
        Color.Grey)
      _drawTextLabel(
        List(TypeDescriber.describeUnitType(trackedUnit.utype)),
        trackedUnit.position,
        drawBackground = true)
    }
  }
  
  def _drawWorkers() {
    With.units.ours.filter(_.utype.isWorker).foreach(unit =>
      _drawTextLabel(List(unit.order.toString), unit.position, drawBackground = true))
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
  
  def _describePlan(plan:Plan, childOrder:Integer, depth:Integer):String = {
    val checkbox = if (plan.isComplete) "X " else "  "
    val spacer = "  " * depth
    val leftColumn =
      (checkbox
        ++ spacer
        ++ "#"
        ++ (childOrder + 1).toString
        ++ " "
        ++ plan.toString)
    
    leftColumn + " " * Math.max(0, 45 - leftColumn.length) + "\n"
  }
  
  def _isRelevant(plan:Plan):Boolean = {
    if (plan.isComplete) {
      return plan.isInstanceOf[LockCurrency] || plan.isInstanceOf[LockUnits]
    }
    
    plan.getChildren.exists(_isRelevant(_))
  }
  
  def _drawTextLabel(
    textLines:Iterable[String],
    position:Position,
    drawBackground:Boolean = false,
    backgroundColor:Color = Color.Grey) {
    val horizontalMargin = 2
    val estimatedTextWidth = (9 * textLines.map(_.size).max) / 2
    val boxWidth = estimatedTextWidth + 2 * horizontalMargin
    val boxHeight = 11 * textLines.size
    val textX = position.getX - boxWidth/2
    val textY = position.getY - boxHeight/2
    val boxX = textX - horizontalMargin
    val boxY = textY
    
    if (drawBackground) {
      With.game.drawBoxMap(
        boxX,
        boxY,
        boxX + boxWidth,
        boxY + boxHeight,
        backgroundColor,
        true) //isSolid
    }
    With.game.drawTextMap(
      textX,
      textY,
      textLines.mkString("\n"))
  }
  
  def _drawTileRectangle(rectangle:TileRectangle, color:Color) {
    With.game.drawBoxMap(rectangle.startPosition, rectangle.endPosition, color)
  }
  
  def _drawPolygonPositions(points:Iterable[Position], color:bwapi.Color = bwapi.Color.Brown) {
    points.reduce((p1, p2) => { With.game.drawLineMap(p1, p2, color); p2 })
    With.game.drawLineMap(points.head, points.last, color)
  }
}
