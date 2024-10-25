package Gameplans.Protoss.PvT

import Lifecycle.With
import Macro.Facts.MacroCounting
import Mathematics.Maff
import Strategery.Strategies.Protoss.{PvT1015, PvT13Nexus, PvT1BaseReaver, PvT28Nexus, PvT29Arbiter, PvT4Gate, PvT910, PvTDT, PvTRangeless, PvTZZCoreZ, PvTZealotExpand}

class PvTEcoScore extends MacroCounting {

  var us        = 0d
  var foe       = 0d
  var scoredFoe = false

  def delta: Double = us - foe

  private def scoreUs(value: Double, matches: Boolean): Unit = {
    us += Maff.or0(value, matches)
  }
  private def scoreFoe(value: Double, matches: Boolean): Unit = {
    scoredFoe ||= matches
    foe += Maff.or0(value, matches)
  }

  scoreUs(   4, PvT13Nexus())
  scoreUs(   2, PvTZealotExpand())
  scoreUs(   1, PvTRangeless())
  scoreUs(   0, PvT28Nexus())
  scoreUs(  -1, PvTZZCoreZ())
  scoreUs(  -1, PvTDT())
  scoreUs(  -2, PvT1BaseReaver())
  scoreUs(  -2, PvT29Arbiter())
  scoreUs(  -3, PvT1015())
  scoreUs(  -4, PvT4Gate())
  scoreUs(  -5, PvT910())
  scoreFoe(  4, With.fingerprints.fourteenCC())
  scoreFoe(  2, With.fingerprints.oneRaxFE())
  scoreFoe(  0, With.fingerprints.oneFac() && ! With.fingerprints.fd()) // Includes siege expand
  scoreFoe( -1, With.fingerprints.fd())
  scoreFoe( -2, With.fingerprints.twoFac())
  scoreFoe( -2, With.fingerprints.twoRax1113())
  scoreFoe( -3, With.fingerprints.threeFac())
  scoreFoe( -4, With.fingerprints.twoRaxAcad())
  scoreFoe( -6, With.fingerprints.bbs())
}
