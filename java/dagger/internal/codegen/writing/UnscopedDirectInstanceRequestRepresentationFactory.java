/*
 * Copyright (C) 2021 The Dagger Authors.
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

import dagger.internal.codegen.binding.ComponentRequirement;
import dagger.internal.codegen.binding.ContributionBinding;
import dagger.internal.codegen.binding.ProvisionBinding;
import dagger.spi.model.RequestKind;
import javax.inject.Inject;

/**
 * A factory for creating a binding expression for an unscoped instance.
 *
 * <p>Note that these binding expressions are for getting "direct" instances -- i.e. instances that
 * are created via constructors or modules (e.g. {@code new Foo()} or {@code
 * FooModule.provideFoo()}) as opposed to an instance created from calling a getter on a framework
 * type (e.g. {@code fooProvider.get()}). See {@link FrameworkInstanceRequestRepresentation} for
 * binding expressions that are created from framework types.
 */
final class UnscopedDirectInstanceRequestRepresentationFactory {
  private final AssistedFactoryRequestRepresentation.Factory
      assistedFactoryRequestRepresentationFactory;
  private final ComponentInstanceRequestRepresentation.Factory
      componentInstanceRequestRepresentationFactory;
  private final ComponentProvisionRequestRepresentation.Factory
      componentProvisionRequestRepresentationFactory;
  private final ComponentRequirementRequestRepresentation.Factory
      componentRequirementRequestRepresentationFactory;
  private final DelegateRequestRepresentation.Factory delegateRequestRepresentationFactory;
  private final MapRequestRepresentation.Factory mapRequestRepresentationFactory;
  private final OptionalRequestRepresentation.Factory optionalRequestRepresentationFactory;
  private final SetRequestRepresentation.Factory setRequestRepresentationFactory;
  private final SimpleMethodRequestRepresentation.Factory simpleMethodRequestRepresentationFactory;
  private final SubcomponentCreatorRequestRepresentation.Factory
      subcomponentCreatorRequestRepresentationFactory;

  @Inject
  UnscopedDirectInstanceRequestRepresentationFactory(
      ComponentImplementation componentImplementation,
      AssistedFactoryRequestRepresentation.Factory assistedFactoryRequestRepresentationFactory,
      ComponentInstanceRequestRepresentation.Factory componentInstanceRequestRepresentationFactory,
      ComponentProvisionRequestRepresentation.Factory
          componentProvisionRequestRepresentationFactory,
      ComponentRequirementRequestRepresentation.Factory
          componentRequirementRequestRepresentationFactory,
      DelegateRequestRepresentation.Factory delegateRequestRepresentationFactory,
      MapRequestRepresentation.Factory mapRequestRepresentationFactory,
      OptionalRequestRepresentation.Factory optionalRequestRepresentationFactory,
      SetRequestRepresentation.Factory setRequestRepresentationFactory,
      SimpleMethodRequestRepresentation.Factory simpleMethodRequestRepresentationFactory,
      SubcomponentCreatorRequestRepresentation.Factory
          subcomponentCreatorRequestRepresentationFactory) {
    this.assistedFactoryRequestRepresentationFactory = assistedFactoryRequestRepresentationFactory;
    this.componentInstanceRequestRepresentationFactory =
        componentInstanceRequestRepresentationFactory;
    this.componentProvisionRequestRepresentationFactory =
        componentProvisionRequestRepresentationFactory;
    this.componentRequirementRequestRepresentationFactory =
        componentRequirementRequestRepresentationFactory;
    this.delegateRequestRepresentationFactory = delegateRequestRepresentationFactory;
    this.mapRequestRepresentationFactory = mapRequestRepresentationFactory;
    this.optionalRequestRepresentationFactory = optionalRequestRepresentationFactory;
    this.setRequestRepresentationFactory = setRequestRepresentationFactory;
    this.simpleMethodRequestRepresentationFactory = simpleMethodRequestRepresentationFactory;
    this.subcomponentCreatorRequestRepresentationFactory =
        subcomponentCreatorRequestRepresentationFactory;
  }

  /** Returns a direct, unscoped binding expression for a {@link RequestKind#INSTANCE} request. */
  RequestRepresentation create(ContributionBinding binding) {
    switch (binding.kind()) {
      case DELEGATE:
        return delegateRequestRepresentationFactory.create(binding, RequestKind.INSTANCE);

      case COMPONENT:
        return componentInstanceRequestRepresentationFactory.create(binding);

      case COMPONENT_DEPENDENCY:
        return componentRequirementRequestRepresentationFactory.create(
            binding, ComponentRequirement.forDependency(binding.key().type().java()));

      case COMPONENT_PROVISION:
        return componentProvisionRequestRepresentationFactory.create((ProvisionBinding) binding);

      case SUBCOMPONENT_CREATOR:
        return subcomponentCreatorRequestRepresentationFactory.create(binding);

      case MULTIBOUND_SET:
        return setRequestRepresentationFactory.create((ProvisionBinding) binding);

      case MULTIBOUND_MAP:
        return mapRequestRepresentationFactory.create((ProvisionBinding) binding);

      case OPTIONAL:
        return optionalRequestRepresentationFactory.create((ProvisionBinding) binding);

      case BOUND_INSTANCE:
        return componentRequirementRequestRepresentationFactory.create(
            binding, ComponentRequirement.forBoundInstance(binding));

      case ASSISTED_FACTORY:
        return assistedFactoryRequestRepresentationFactory.create((ProvisionBinding) binding);

      case ASSISTED_INJECTION:
      case INJECTION:
      case PROVISION:
        return simpleMethodRequestRepresentationFactory.create((ProvisionBinding) binding);

      case MEMBERS_INJECTOR:
      case MEMBERS_INJECTION:
      case COMPONENT_PRODUCTION:
      case PRODUCTION:
        // Fall through
    }
    throw new AssertionError("Unexpected binding kind: " + binding.kind());
  }
}
