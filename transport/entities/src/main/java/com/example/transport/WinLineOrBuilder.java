// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: base.proto

package com.example.transport;

public interface WinLineOrBuilder extends
    // @@protoc_insertion_point(interface_extends:base.WinLine)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>.base.MarkType mark = 1;</code>
   * @return The enum numeric value on the wire for mark.
   */
  int getMarkValue();
  /**
   * <code>.base.MarkType mark = 1;</code>
   * @return The mark.
   */
  com.example.transport.MarkType getMark();

  /**
   * <code>optional .base.Move start = 2;</code>
   * @return Whether the start field is set.
   */
  boolean hasStart();
  /**
   * <code>optional .base.Move start = 2;</code>
   * @return The start.
   */
  com.example.transport.Move getStart();

  /**
   * <code>optional .base.Move end = 3;</code>
   * @return Whether the end field is set.
   */
  boolean hasEnd();
  /**
   * <code>optional .base.Move end = 3;</code>
   * @return The end.
   */
  com.example.transport.Move getEnd();
}
