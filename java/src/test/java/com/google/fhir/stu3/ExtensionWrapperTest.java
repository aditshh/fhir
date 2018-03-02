//    Copyright 2018 Google Inc.
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//        https://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.

package com.google.fhir.stu3;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.io.Files;
import com.google.fhir.stu3.proto.Extension;
import com.google.fhir.stu3.proto.PrimitiveHasNoValue;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link ExtensionWrapper}. */
@RunWith(JUnit4.class)
public final class ExtensionWrapperTest {

  TextFormat.Parser textParser;

  /** Read the specifed prototxt file from the testdata directory and parse it. */
  private void mergeText(java.lang.String filename, Message.Builder builder) throws IOException {
    File file = new File("testdata/stu3/extensions/" + filename);
    textParser.merge(Files.asCharSource(file, StandardCharsets.UTF_8).read(), builder);
  }

  private <T extends Message> void testMerge(java.lang.String name, T template) throws IOException {
    // Parse the input.
    Extension.Builder extensionBuilder = Extension.newBuilder();
    Message.Builder goldenBuilder = template.newBuilderForType();
    mergeText(name + ".message.prototxt", goldenBuilder);
    mergeText(name + ".extension.prototxt", extensionBuilder);

    java.util.List<T> messages =
        ExtensionWrapper.of(Collections.singletonList(extensionBuilder.build()))
            .getMatchingExtensions(template);
    assertThat(messages).containsExactly(goldenBuilder.build());
  }

  private void testExpand(java.lang.String name, Message.Builder builder) throws IOException {
    // Parse the input.
    Extension.Builder extensionBuilder = Extension.newBuilder();
    mergeText(name + ".message.prototxt", builder);
    mergeText(name + ".extension.prototxt", extensionBuilder);

    java.util.List<Extension> result = ExtensionWrapper.of().add(builder).build();
    assertThat(result).containsExactly(extensionBuilder.build());
  }

  @Before
  public void setUp() {
    textParser = TextFormat.getParser();
  }

  /** Test merging of an Extension into a PrimitiveHasNoValue message. */
  @Test
  public void mergePrimitiveHasNoValue() throws Exception {
    testMerge("primitive_has_no_value", PrimitiveHasNoValue.getDefaultInstance());
    testMerge("empty", PrimitiveHasNoValue.getDefaultInstance());
  }

  /** Test expanding a PrimitiveHasNoValue into an Extension. */
  @Test
  public void expandPrimitiveHasNoValue() throws Exception {
    testExpand("primitive_has_no_value", PrimitiveHasNoValue.newBuilder());
    testExpand("empty", PrimitiveHasNoValue.newBuilder());
  }
}
