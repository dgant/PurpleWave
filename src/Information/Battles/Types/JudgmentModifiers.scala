package Information.Battles.Types

import scala.collection.mutable.ArrayBuffer

object JudgmentModifiers {
  def apply(battle: BattleLocal): Seq[JudgmentModifier] = {
    val output = new ArrayBuffer[JudgmentModifier]
    def add(name: String, modifier: Option[JudgmentModifier]) {
      modifier.foreach(m => {
        m.name = name
        output += m })
    }
    add("Proximity",    proximity(battle))
    add("Coherence",    coherence(battle))
    add("EnemyChoked",  enemyChoked(battle))
    add("Rout",         rout(battle))
    add("Maxed",        maxed(battle))
    add("Gatherers",    gatherers(battle))
    add("HornetNest",   hornetNest(battle))
    add("Commitment",   commitment(battle))
    add("Patience",     patience(battle))
    output
  }

  // Prefer fighting
  //  when close to home,
  //  especially if pushed into our main/natural
  //    because we will run out of room to retreat
  //    and because workers or buildings will be endangered if we don't
  def proximity(battleLocal: BattleLocal): Option[JudgmentModifier] = {
    None
  }

  // Prefer fighting
  //  when we have a coherence advantage,
  //    because the benefits of this are underrepresented in simulation
  //      due to the absence of collisions
  def coherence(battleLocal: BattleLocal): Option[JudgmentModifier] = {
    None
  }

  // Prefer fighting
  //  when the enemy is entering a choke
  //    because the benefits of this are underrepresented in simulation
  //      due to the absence of collisions,
  //    and because the situation is likely to get worse for us
  //      once they have gotten through the choke
  def enemyChoked(battleLocal: BattleLocal): Option[JudgmentModifier] = {
    None
  }

  // Prefer fighting
  //   when the enemy is running away
  //     because we can get free damage in
  //     and because perhaps the fight is good for us
  //       due to reasons we haven't considered
  def rout(battleLocal: BattleLocal): Option[JudgmentModifier] = {
    None
  }

  // Prefer fighting
  //   when we are maxed out
  //   especially with a bank
  //     because from here the enemy will only get stronger relative to us
  def maxed(battleLocal: BattleLocal): Option[JudgmentModifier] = {
    None
  }

  // Prefer fighting
  //   when our gatherers are endangered
  //    because they are very fragile
  //    and if they die we will probably lose the game
  def gatherers(battleLocal: BattleLocal): Option[JudgmentModifier] = {
    None
  }

  // Avoid fighting
  //   into an enemy base that is likely to have reinforcements
  //   especially if those reinforcements are siege tanks
  //     because their existence is highly probable
  //     and if we attacked in error once, we will likely keep doing it
  //     and thus systematically bleed units
  def hornetNest(battleLocal: BattleLocal): Option[JudgmentModifier] = {
    None
  }

  // Avoid disengaging
  //   from a fight we have already committed to
  //   especially if it is a large battle
  //     because leaving a battle is costly
  //     and if it's a large battle, our reinforcements won't make up
  //       for the units we lose in the retreat
  def commitment(battleLocal: BattleLocal): Option[JudgmentModifier] = {
    None
  }

  // Avoid engaging
  //   in a fight that's not obviously winnable
  //   until we have gained confidence over time that it will be winnable
  //     because high-frequency combat vacillation
  //     systematically causes more bad decisions and bleeding
  def patience(battleLocal: BattleLocal): Option[JudgmentModifier] = {
    None
  }
}
