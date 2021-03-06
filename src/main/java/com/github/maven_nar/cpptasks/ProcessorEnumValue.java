/*
 * #%L
 * Native ARchive plugin for Maven
 * %%
 * Copyright (C) 2002 - 2014 NAR Maven Plugin developers.
 * %%
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
 * #L%
 */
package com.github.maven_nar.cpptasks;

import com.github.maven_nar.cpptasks.compiler.Processor;

/**
 * One entry in the arrays used by the CompilerEnum and LinkerEnum classes.
 *
 * @author Curt Arnold
 * @see CompilerEnum
 * @see LinkerEnum
 * 
 */
public class ProcessorEnumValue {
  public static String[] getValues(final ProcessorEnumValue[] processors) {
    final String[] values = new String[processors.length];
    for (int i = 0; i < processors.length; i++) {
      values[i] = processors[i].getName();
    }
    return values;
  }

  private final String name;
  private final Processor processor;

  public ProcessorEnumValue(final String name, final Processor processor) {
    this.name = name;
    this.processor = processor;
  }

  public String getName() {
    return this.name;
  }

  public Processor getProcessor() {
    return this.processor;
  }
}
