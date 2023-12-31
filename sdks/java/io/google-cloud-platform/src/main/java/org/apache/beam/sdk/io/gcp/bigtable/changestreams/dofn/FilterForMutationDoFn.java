/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.beam.sdk.io.gcp.bigtable.changestreams.dofn;

import com.google.cloud.bigtable.data.v2.models.ChangeStreamMutation;
import com.google.cloud.bigtable.data.v2.models.ChangeStreamRecord;
import com.google.protobuf.ByteString;
import java.io.Serializable;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;

public class FilterForMutationDoFn
    extends DoFn<KV<ByteString, ChangeStreamRecord>, KV<ByteString, ChangeStreamMutation>>
    implements Serializable {

  @ProcessElement
  public void processElement(
      @Element KV<ByteString, ChangeStreamRecord> changeStreamRecordKV,
      OutputReceiver<KV<ByteString, ChangeStreamMutation>> receiver) {
    ChangeStreamRecord inputRecord = changeStreamRecordKV.getValue();
    if (inputRecord instanceof ChangeStreamMutation) {
      receiver.output(KV.of(changeStreamRecordKV.getKey(), (ChangeStreamMutation) inputRecord));
    }
  }
}
