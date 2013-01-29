package trafficland.opensource.sbt.plugins.releasemanagement

import sbt.SettingKey

abstract class ReleaseType(){

  protected val appReleaseTasks : SettingKey[Seq[String]]
  protected val libReleaseTasks : SettingKey[Seq[String]]

  def getReleaseTasks(isApp:Boolean) : SettingKey[Seq[String]] = {
    isApp match {
      case true => appReleaseTasks
      case false => libReleaseTasks
    }
  }

  def isValidReleaseVersion(version:String) : Boolean
}
