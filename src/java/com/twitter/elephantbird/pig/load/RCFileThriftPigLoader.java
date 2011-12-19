package com.twitter.elephantbird.pig.load;

import java.io.IOException;

import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.pig.backend.hadoop.executionengine.mapReduceLayer.PigSplit;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import com.twitter.elephantbird.mapreduce.input.RCFileThriftInputFormat;
import com.twitter.elephantbird.pig.util.RCFileUtil;
import com.twitter.elephantbird.util.TypeRef;

public class RCFileThriftPigLoader extends LzoThriftB64LinePigLoader<TBase<?,?>> {

  private RCFileThriftInputFormat.ThriftReader thriftReader;

  /**
   * @param thriftClassName fully qualified name of the thrift class
   */
  public RCFileThriftPigLoader(String thriftClassName) {
    super(thriftClassName);
  }

  @Override @SuppressWarnings("unchecked")
  public InputFormat getInputFormat() throws IOException {
    return new RCFileThriftInputFormat(typeRef_);
  }

  @SuppressWarnings("unchecked")
  protected <M> M getNextBinaryValue(TypeRef<M> typeRef) throws IOException {
    try {
      if (thriftReader.nextKeyValue()) {
        return (M) thriftReader.getCurrentThriftValue();
      }
    } catch (TException e) {
      throw new IOException(e);
    } catch (InterruptedException e) {
      throw new IOException(e);
    }

    return null;
  }

  @Override @SuppressWarnings("unchecked")
  public void prepareToRead(RecordReader reader, PigSplit split) {
    // pass null so that, there is no way it could be misused
    super.prepareToRead(null, split);
    thriftReader = (RCFileThriftInputFormat.ThriftReader) reader;
  }

  @Override
  public void setLocation(String location, Job job) throws IOException {
    super.setLocation(location, job);
    RCFileUtil.setRequiredFieldConf(job.getConfiguration(),
                                    requiredFieldList);
  }

}

