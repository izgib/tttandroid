// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: bluetooth.proto

package com.example.transport;

public interface BluetoothCreatorMsgOrBuilder extends
    // @@protoc_insertion_point(interface_extends:BluetoothCreatorMsg)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>.base.GameStatus status = 1;</code>
   * @return Whether the status field is set.
   */
  boolean hasStatus();
  /**
   * <code>.base.GameStatus status = 1;</code>
   * @return The enum numeric value on the wire for status.
   */
  int getStatusValue();
  /**
   * <code>.base.GameStatus status = 1;</code>
   * @return The status.
   */
  com.example.transport.GameStatus getStatus();

  /**
   * <code>.base.WinLine win_line = 2;</code>
   * @return Whether the winLine field is set.
   */
  boolean hasWinLine();
  /**
   * <code>.base.WinLine win_line = 2;</code>
   * @return The winLine.
   */
  com.example.transport.WinLine getWinLine();

  /**
   * <code>.base.StopCause cause = 3;</code>
   * @return Whether the cause field is set.
   */
  boolean hasCause();
  /**
   * <code>.base.StopCause cause = 3;</code>
   * @return The enum numeric value on the wire for cause.
   */
  int getCauseValue();
  /**
   * <code>.base.StopCause cause = 3;</code>
   * @return The cause.
   */
  com.example.transport.StopCause getCause();

  /**
   * <code>.base.GameParams params = 4;</code>
   * @return Whether the params field is set.
   */
  boolean hasParams();
  /**
   * <code>.base.GameParams params = 4;</code>
   * @return The params.
   */
  com.example.transport.GameParams getParams();

  /**
   * <code>optional .base.Move move = 5;</code>
   * @return Whether the move field is set.
   */
  boolean hasMove();
  /**
   * <code>optional .base.Move move = 5;</code>
   * @return The move.
   */
  com.example.transport.Move getMove();

  public com.example.transport.BluetoothCreatorMsg.PayloadCase getPayloadCase();
}