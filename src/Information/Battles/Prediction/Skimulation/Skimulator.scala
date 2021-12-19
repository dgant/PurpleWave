package Information.Battles.Prediction.Skimulation

import Information.Battles.Types.BattleLocal
import Mathematics.Maff
import ProxyBwapi.Races.{Protoss, Terran, Zerg}

object Skimulator {

  def predict(battle: BattleLocal): Unit = {
    // Calculate unit properties
    battle.teams.foreach(team => team.units.foreach(unit => {
      val energy = if (unit.isFriendly) unit.energy else 100
      val player = unit.player
      val casts75 = Math.floor(unit.energy / 75)
      val casts100 = Math.floor(unit.energy / 100)
      val casts125 = Math.floor(unit.energy / 125)
      val casts150 = Math.floor(unit.energy / 150)
      //TODO: Splash value
      //TODO: Medic value
      //TODO: Integrate judgments
      //TODO: Consider weighing HP over shields
      //TODO: Enshuttled Reavers
      unit.skimStrength = Maff.nanToZero(unit.unitClass.skimulationValue * unit.totalHealth / unit.unitClass.maxTotalHealth)
      unit.skimStrength *= (if (Protoss.Reaver(unit) && unit.isFriendly) unit.interceptors.size / 8.0 else 1.0)

      // Count basic upgrades
      unit.skimStrength *= 1.0 + 0.15 * (unit.armorHealth - unit.unitClass.armor)
      if (unit.unitClass.effectiveAirDamage > 0)
        unit.skimStrength *= unit.damageOnHitAir / unit.unitClass.effectiveAirDamage
      else if (unit.unitClass.effectiveGroundDamage> 0)
        unit.skimStrength *= unit.damageOnHitGround / unit.unitClass.effectiveGroundDamage

      // Consider high ground advantage
      if (unit.isFriendly && unit.canAttack && unit.effectiveRangePixels > 64 && ! unit.flying) {
        if (unit.presumptiveTarget.exists(_.altitude > unit.altitude)) {
          unit.skimStrength *= 0.5
        } else if (unit.presumptiveTarget.exists(t => t.altitude < unit.pixelToFireAt(t).altitude)) {
          unit.skimStrength *= 1.5
        }
      }

      // Consider detection
      if ( ! team.opponent.detectors.exists(d => d.canMove || d.isEnemy)) {
        if (Terran.Wraith(unit) && Terran.WraithCloak(player)) unit.skimStrength *= 5
        if (Protoss.DarkTemplar(unit)) unit.skimStrength *= 15
        if (Zerg.Lurker(unit)) unit.skimStrength *= 5
      }

      // Count other unit properties
      if ( ! unit.complete)                                               unit.skimStrength *= 0
      if (unit.canStim)                                                   unit.skimStrength *= 1.2
      if (Terran.Marine(unit)   && Terran.MarineRange(player))            unit.skimStrength *= 1.2
      if (Terran.Medic(unit))                                             unit.skimStrength *= 1.0 // TODO: Cap on bio allies
      if (Protoss.Carrier(unit) && unit.isFriendly)                       unit.skimStrength *= unit.interceptors.size / 8.0
      if (Protoss.Reaver(unit)  && unit.isFriendly && unit.scarabs == 0)  unit.skimStrength *= 0
      if (Protoss.Zealot(unit)  && Protoss.ZealotSpeed(player))           unit.skimStrength *= 1.2
      if (Protoss.Dragoon(unit) && Protoss.DragoonRange(player))          unit.skimStrength *= 1.5
      if (Zerg.Lurker(unit)     && ! unit.burrowed)                       unit.skimStrength *= 0.8
      if (Zerg.Zergling(unit)   && Zerg.ZerglingSpeed(player))            unit.skimStrength *= 1.2
      if (Zerg.Zergling(unit)   && Zerg.ZerglingAttackSpeed(player))      unit.skimStrength *= 1.2
      if (Zerg.Hydralisk(unit)  && Zerg.HydraliskSpeed(player))           unit.skimStrength *= 1.2
      if (Zerg.Hydralisk(unit)  && Zerg.HydraliskRange(player))           unit.skimStrength *= 1.2

      // Count spells
      unit.skimMagic = 0
      unit.skimMagic = Math.max(unit.skimMagic, Maff.fromBoolean(Terran.ScienceVessel(unit))                                  * casts100  * 1.5)
      unit.skimMagic = Math.max(unit.skimMagic, Maff.fromBoolean(Terran.Battlecruiser(unit))                                  * casts150  * 2.5) // TODO: Max value
      unit.skimMagic = Math.max(unit.skimMagic, Maff.fromBoolean(Terran.Ghost(unit)         && Terran.Lockdown(player))       * casts100  * 2.5) // TODO: Max mechanical value
      unit.skimMagic = Math.max(unit.skimMagic, Maff.fromBoolean(Protoss.HighTemplar(unit)  && Protoss.PsionicStorm(player))  * casts75   * 2.5) // TODO: Capped by fraction of total value
      unit.skimMagic = Math.max(unit.skimMagic, Maff.fromBoolean(Protoss.DarkArchon(unit)   && Protoss.Maelstrom(player))     * casts100  * 4)   // TODO: Capped by max bio value
      unit.skimMagic = Math.max(unit.skimMagic, Maff.fromBoolean(Protoss.Arbiter(unit)      && Protoss.Stasis(player))        * casts100  * 5)   // TODO: Capped by half max value
      unit.skimMagic = Math.max(unit.skimMagic, Maff.fromBoolean(Zerg.Queen(unit)           && Zerg.Ensnare(player))          * casts100  * 6)   // TODO: Capped by team melee valud
      unit.skimMagic = Math.max(unit.skimMagic, Maff.fromBoolean(Zerg.Defiler(unit))                                          * casts100  * 6)   // TODO: Capped by team melee valud
      unit.skimStrength += unit.skimMagic

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
    // TODO: Terrain influence: Choke
    // TODO: Terrain influence: High Ground
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

    battle.predictionComplete = true
  }
}
