package com.ing.baker.runtime.model

import cats.implicits._
import cats.effect.{ConcurrentEffect, ContextShift, IO}
import com.ing.baker.il.petrinet.InteractionTransition
import com.ing.baker.runtime.scaladsl.{EventInstance, IngredientInstance, InteractionInstance}

abstract class InteractionInstanceManager[F[_]](implicit eff: ConcurrentEffect[F], contextShift: ContextShift[IO]) {

  def add(interaction: InteractionInstance): F[Unit]

  def get(interaction: InteractionTransition): F[Option[InteractionInstance]]

  def contains(interaction: InteractionTransition): F[Boolean] =
    get(interaction).map(_.isDefined)

  def execute(interaction: InteractionTransition, input: Seq[IngredientInstance]): F[Option[EventInstance]] = {
    get(interaction).flatMap {
      case Some(implementation) => eff.liftIO(IO.fromFuture(IO(implementation.run(input))))
      case None => eff.raiseError(new IllegalStateException(s"No implementation available for interaction ${interaction.interactionName}"))
    }
  }

  protected def isCompatible(interaction: InteractionTransition, implementation: InteractionInstance): Boolean = {
    val interactionNameMatches =
      interaction.originalInteractionName == implementation.name
    val inputSizeMatches =
      implementation.input.size == interaction.requiredIngredients.size
    val inputNamesAndTypesMatches =
      interaction
        .requiredIngredients
        .forall { descriptor =>
          implementation.input.exists(_.isAssignableFrom(descriptor.`type`))
        }
    interactionNameMatches && inputSizeMatches && inputNamesAndTypesMatches
  }
}
