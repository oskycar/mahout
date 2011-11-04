/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.mahout.cf.taste.hadoop.als;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.mahout.common.Pair;
import org.apache.mahout.common.iterator.sequencefile.PathFilters;
import org.apache.mahout.common.iterator.sequencefile.PathType;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileDirIterable;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;
import org.apache.mahout.math.map.OpenIntObjectHashMap;


import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class ALSUtils {

  private ALSUtils() {}

  static Vector readFirstRow(Path dir, Configuration conf) throws IOException {
    Vector v = null;
    for (Pair<IntWritable,VectorWritable> pair :
        new SequenceFileDirIterable<IntWritable,VectorWritable>(dir, PathType.LIST, PathFilters.partFilter(), conf)) {
      v = pair.getSecond().get();
      break;
    }
    return v;
  }

  static OpenIntObjectHashMap<Vector> readMatrixByRows(Path dir, Configuration conf) throws IOException {
    OpenIntObjectHashMap<Vector> matrix = new OpenIntObjectHashMap<Vector>();

    for (Pair<IntWritable,VectorWritable> pair :
        new SequenceFileDirIterable<IntWritable,VectorWritable>(dir, PathType.LIST, PathFilters.partFilter(), conf)) {
      int rowIndex = pair.getFirst().get();
      Vector row = pair.getSecond().get().clone();
      matrix.put(rowIndex, row);
    }
    return matrix;
  }

  public static String nice(Vector v) {
    if (!v.isSequentialAccess()) {
      v = new DenseVector(v);
    }

    DecimalFormat df = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

    StringBuilder buffer = new StringBuilder("[");
    String separator = "";
    for (Vector.Element e : v) {
      buffer.append(separator);
      if (!Double.isNaN(e.get())) {
        if (e.get() >= 0) {
          buffer.append(" ");
        }
        buffer.append(df.format(e.get()));
      } else {
        buffer.append("  -  ");
      }
      separator = "\t";
    }
    buffer.append(" ]");
    return buffer.toString();
  }
}
