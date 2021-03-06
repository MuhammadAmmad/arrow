package arrow.data

import arrow.Kind
import arrow.core.*
import arrow.mtl.functorFilter
import arrow.mtl.traverseFilter
import arrow.test.UnitSpec
import arrow.test.laws.*
import arrow.typeclasses.*
import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.shouldNotBe
import io.kotlintest.properties.forAll
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class OptionTTest : UnitSpec() {

    fun <A> EQ(): Eq<Kind<OptionTPartialOf<A>, Int>> = Eq { a, b ->
        a.value() == b.value()
    }

    fun <A> EQ_NESTED(): Eq<Kind<OptionTPartialOf<A>, Kind<OptionTPartialOf<A>, Int>>> = Eq { a, b ->
        a.value() == b.value()
    }

    val NELM: Monad<ForNonEmptyList> = monad<ForNonEmptyList>()

    init {

        "instances can be resolved implicitly" {
            functor<OptionTPartialOf<ForNonEmptyList>>() shouldNotBe null
            applicative<OptionTPartialOf<ForNonEmptyList>>() shouldNotBe null
            monad<OptionTPartialOf<ForNonEmptyList>>() shouldNotBe null
            foldable<OptionTPartialOf<ForNonEmptyList>>() shouldNotBe null
            traverse<OptionTPartialOf<ForNonEmptyList>>() shouldNotBe null
            semigroupK<OptionTPartialOf<ForListK>>() shouldNotBe null
            monoidK<OptionTPartialOf<ForListK>>() shouldNotBe null
            functorFilter<OptionTPartialOf<ForListK>>() shouldNotBe null
            traverseFilter<OptionTPartialOf<ForOption>>() shouldNotBe null
        }

        testLaws(
            MonadLaws.laws(OptionT.monad(NonEmptyList.monad()), Eq.any()),
            SemigroupKLaws.laws(
                OptionT.semigroupK(Id.monad()),
                OptionT.applicative(Id.monad()),
                EQ()),

            MonoidKLaws.laws(
                OptionT.monoidK(Id.monad()),
                OptionT.applicative(Id.monad()),
                EQ()),

            FunctorFilterLaws.laws(
                OptionT.functorFilter(),
                { OptionT(Id(Some(it))) },
                EQ()),

            TraverseFilterLaws.laws(
                OptionT.traverseFilter(),
                OptionT.applicative(Option.monad()),
                { OptionT(Option(Some(it))) },
                EQ(),
                EQ_NESTED())
        )

        "toLeft for Some should build a correct EitherT" {
            forAll { a: Int, b: String ->
                OptionT.fromOption<ForNonEmptyList, Int>(Some(a)).toLeft({ b }, NELM) == EitherT.left<ForNonEmptyList, Int, String>(a, applicative())
            }
        }

        "toLeft for None should build a correct EitherT" {
            forAll { a: Int, b: String ->
                OptionT.fromOption<ForNonEmptyList, Int>(None).toLeft({ b }, NELM) == EitherT.right<ForNonEmptyList, Int, String>(b, applicative())
            }
        }

        "toRight for Some should build a correct EitherT" {
            forAll { a: Int, b: String ->
                OptionT.fromOption<ForNonEmptyList, String>(Some(b)).toRight({ a }, NELM) == EitherT.right<ForNonEmptyList, Int, String>(b, applicative())
            }
        }

        "toRight for None should build a correct EitherT" {
            forAll { a: Int, b: String ->
                OptionT.fromOption<ForNonEmptyList, String>(None).toRight({ a }, NELM) == EitherT.left<ForNonEmptyList, Int, String>(a, applicative())
            }
        }

    }
}
