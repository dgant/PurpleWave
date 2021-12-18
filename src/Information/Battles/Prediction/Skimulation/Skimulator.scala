package Information.Battles.Prediction.Skimulation

import Information.Battles.Types.BattleLocal
import Mathematics.Maff

object Skimulator {

  def predict(battle: BattleLocal): Unit = {
    // Calculate unit properties
    battle.teams.foreach(team => team.units.foreach(unit => {
      unit.skimValueHealthy = unit.unitClass.skimulationValue
      //TODO: Spell value
      //TODO: Medic value
      //TODO: Interceptor count
      //TODO: Scarab count
      //TODO: Incomplete buildings
      //TODO: Unburrowed lurkers
      //TODO: Stim/Speed/Upgrades
      //TODO: Integrate judgments
      //TODO: Consider 3:1 HP:Shields weighing
      unit.skimStrength = Maff.nanToZero(unit.skimValueHealthy * unit.totalHealth / unit.unitClass.maxTotalHealth)

      val requireTarget = unit.canAttack
      val enemies = team.opponent.units.view

      // TODO: Consider: Does this achieve everything the reference logic does?
      // TODO: Definitely doesn't include ground distance considerations, which for some reason are enemy->friendly and not the reverse
      unit.skimDistanceToEngage = Math.max(0d,
        Maff.min(
          team.opponent.units.map(e =>
            unit.pixelDistanceEdge(e)
              - Math.max(
              if (e.canAttack(unit)) e.pixelRangeAgainst(unit) else 0,
              if (unit.canAttack(e)) unit.pixelRangeAgainst(e) else 0))).getOrElse(0d))
    }))

    // Calculate team properties
    battle.teams.foreach(team => {
      team.skimUnitsClosest = team.units.sortBy(_.skimDistanceToEngage)
      team.skimFrontStart = team.skimUnitsClosest.indices.find(team.units(_).canMove).getOrElse(team.skimUnitsClosest.length)
      if (team.skimFrontStart < team.skimUnitsClosest.length) {
        team.skimFrontEnd = team.skimFrontStart
        var maxGap = 64
        val startDistance = team.skimUnitsClosest(team.skimFrontStart).skimDistanceToEngage
        while (team.skimFrontEnd < team.skimUnitsClosest.length && team.skimUnitsClosest(team.skimFrontEnd).skimDistanceToEngage < startDistance + maxGap) {
          val unit = team.skimUnitsClosest(team.skimFrontEnd)
          team.skimStrengthTotal    += unit.skimStrength
          team.skimStrengthAir      += unit.skimStrength * Maff.fromBoolean(unit.flying)
          team.skimStrengthGround   += unit.skimStrength * Maff.fromBoolean(! unit.flying)
          team.skimStrengthVsAir    += unit.skimStrength * Maff.fromBoolean(unit.canAttackAir || ! unit.canAttack)
          team.skimStrengthVsGround += unit.skimStrength * Maff.fromBoolean(unit.canAttackGround || ! unit.canAttack)
          team.skimFrontEnd += 1
          maxGap = Math.min(maxGap + 32, 32 * 16)
        }
      }
    })

    // TODO: If enemy has undetected DT and we lack detection, abandon ship

    // TODO: Terrain influence

    battle.predictionComplete = true
  }
}
