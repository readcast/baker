package com.ing.baker.runtime.common

import com.ing.baker.il.EventDescriptor
import com.ing.baker.runtime.common.LanguageDataStructures.LanguageApi
import com.ing.baker.types.Value

trait RuntimeEvent extends LanguageApi {

  def name: String

  def providedIngredients: language.Map[String, Value]

  def occurredOn: Long

  def validate(descriptor: EventDescriptor): language.Seq[String]
}
