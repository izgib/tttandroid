// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: network.proto

package com.example.transport;

public interface CreateRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:transport.CreateRequest)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>.base.Move move = 1;</code>
   * @return Whether the move field is set.
   */
  boolean hasMove();
  /**
   * <code>.base.Move move = 1;</code>
   * @return The move.
   */
  com.example.transport.Move getMove();

  /**
   * <code>.base.ClientAction action = 2;</code>
   * @return Whether the action field is set.
   */
  boolean hasAction();
  /**
   * <code>.base.ClientAction action = 2;</code>
   * @return The enum numeric value on the wire for action.
   */
  int getActionValue();
  /**
   * <code>.base.ClientAction action = 2;</code>
   * @return The action.
   */
  com.example.transport.ClientAction getAction();

  /**
   * <code>.base.GameParams params = 3;</code>
   * @return Whether the params field is set.
   */
  boolean hasParams();
  /**
   * <code>.base.GameParams params = 3;</code>
   * @return The params.
   */
  com.example.transport.GameParams getParams();

  public com.example.transport.CreateRequest.PayloadCase getPayloadCase();
}
