/*
 * Copyright (C) 2016 The Dagger Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dagger.internal.codegen.validation;

import static dagger.internal.codegen.validation.BindingElementValidator.AllowsMultibindings.ALLOWS_MULTIBINDINGS;
import static dagger.internal.codegen.validation.BindingElementValidator.AllowsScoping.ALLOWS_SCOPING;
import static dagger.internal.codegen.validation.BindingMethodValidator.Abstractness.MUST_BE_ABSTRACT;
import static dagger.internal.codegen.validation.BindingMethodValidator.ExceptionSuperclass.NO_EXCEPTIONS;
import static dagger.internal.codegen.validation.TypeHierarchyValidator.validateTypeHierarchy;
import static dagger.internal.codegen.xprocessing.XTypes.isPrimitive;

import androidx.room.compiler.processing.XMethodElement;
import androidx.room.compiler.processing.XType;
import androidx.room.compiler.processing.XVariableElement;
import com.google.common.collect.ImmutableSet;
import dagger.internal.codegen.base.ContributionType;
import dagger.internal.codegen.base.SetType;
import dagger.internal.codegen.binding.BindsTypeChecker;
import dagger.internal.codegen.binding.InjectionAnnotations;
import dagger.internal.codegen.javapoet.TypeNames;
import dagger.internal.codegen.langmodel.DaggerTypes;
import javax.inject.Inject;

/** A validator for {@link dagger.Binds} methods. */
final class BindsMethodValidator extends BindingMethodValidator {
  private final DaggerTypes types;
  private final BindsTypeChecker bindsTypeChecker;

  @Inject
  BindsMethodValidator(
      DaggerTypes types,
      BindsTypeChecker bindsTypeChecker,
      DependencyRequestValidator dependencyRequestValidator,
      InjectionAnnotations injectionAnnotations) {
    super(
        types,
        TypeNames.BINDS,
        ImmutableSet.of(TypeNames.MODULE, TypeNames.PRODUCER_MODULE),
        dependencyRequestValidator,
        MUST_BE_ABSTRACT,
        NO_EXCEPTIONS,
        ALLOWS_MULTIBINDINGS,
        ALLOWS_SCOPING,
        injectionAnnotations);
    this.types = types;
    this.bindsTypeChecker = bindsTypeChecker;
  }

  @Override
  protected ElementValidator elementValidator(XMethodElement method) {
    return new Validator(method);
  }

  private class Validator extends MethodValidator {
    private final XMethodElement method;

    Validator(XMethodElement method) {
      super(method);
      this.method = method;
    }

    @Override
    protected void checkParameters() {
      if (method.getParameters().size() != 1) {
        report.addError(
            bindingMethods(
                "must have exactly one parameter, whose type is assignable to the return type"));
      } else {
        super.checkParameters();
      }
    }

    @Override
    protected void checkParameter(XVariableElement parameter) {
      super.checkParameter(parameter);
      XType leftHandSide = boxIfNecessary(method.getReturnType());
      XType rightHandSide = parameter.getType();
      ContributionType contributionType = ContributionType.fromBindingElement(method);
      if (contributionType.equals(ContributionType.SET_VALUES) && !SetType.isSet(leftHandSide)) {
        report.addError(
            "@Binds @ElementsIntoSet methods must return a Set and take a Set parameter");
      }

      if (!bindsTypeChecker.isAssignable(rightHandSide, leftHandSide, contributionType)) {
        // Validate the type hierarchy of both sides to make sure they're both valid.
        // If one of the types isn't valid it means we need to delay validation to the next round.
        // Note: BasicAnnotationProcessor only performs superficial validation on the referenced
        // types within the module. Thus, we're guaranteed that the types in the @Binds method are
        // valid, but it says nothing about their supertypes, which are needed for isAssignable.
        validateTypeHierarchy(leftHandSide, types);
        validateTypeHierarchy(rightHandSide, types);
        // TODO(ronshapiro): clarify this error message for @ElementsIntoSet cases, where the
        // right-hand-side might not be assignable to the left-hand-side, but still compatible with
        // Set.addAll(Collection<? extends E>)
        report.addError("@Binds methods' parameter type must be assignable to the return type");
      }
    }

    private XType boxIfNecessary(XType maybePrimitive) {
      return isPrimitive(maybePrimitive) ? maybePrimitive.boxed() : maybePrimitive;
    }
  }
}
