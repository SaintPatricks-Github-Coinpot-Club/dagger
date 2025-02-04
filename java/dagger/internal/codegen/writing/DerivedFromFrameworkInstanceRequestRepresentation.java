/*
 * Copyright (C) 2018 The Dagger Authors.
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

package dagger.internal.codegen.writing;

import static com.google.common.base.Preconditions.checkNotNull;

import com.squareup.javapoet.ClassName;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import dagger.internal.codegen.binding.ComponentDescriptor.ComponentMethodDescriptor;
import dagger.internal.codegen.binding.FrameworkType;
import dagger.internal.codegen.javapoet.Expression;
import dagger.internal.codegen.langmodel.DaggerTypes;
import dagger.spi.model.RequestKind;

/** A binding expression that depends on a framework instance. */
final class DerivedFromFrameworkInstanceRequestRepresentation extends RequestRepresentation {
  private final RequestRepresentation frameworkRequestRepresentation;
  private final RequestKind requestKind;
  private final FrameworkType frameworkType;
  private final DaggerTypes types;

  @AssistedInject
  DerivedFromFrameworkInstanceRequestRepresentation(
      @Assisted RequestRepresentation frameworkRequestRepresentation,
      @Assisted RequestKind requestKind,
      @Assisted FrameworkType frameworkType,
      DaggerTypes types) {
    this.frameworkRequestRepresentation = checkNotNull(frameworkRequestRepresentation);
    this.requestKind = requestKind;
    this.frameworkType = checkNotNull(frameworkType);
    this.types = types;
  }

  @Override
  Expression getDependencyExpression(ClassName requestingClass) {
    return frameworkType.to(
        requestKind,
        frameworkRequestRepresentation.getDependencyExpression(requestingClass),
        types);
  }

  @Override
  Expression getDependencyExpressionForComponentMethod(
      ComponentMethodDescriptor componentMethod, ComponentImplementation component) {
    Expression frameworkInstance =
        frameworkRequestRepresentation.getDependencyExpressionForComponentMethod(
            componentMethod, component);
    return frameworkType.to(requestKind, frameworkInstance, types);
  }

  @AssistedFactory
  static interface Factory {
    DerivedFromFrameworkInstanceRequestRepresentation create(
        RequestRepresentation frameworkRequestRepresentation,
        RequestKind requestKind,
        FrameworkType frameworkType);
  }
}
