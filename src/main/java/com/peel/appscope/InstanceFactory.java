// Copyright (c) 2018 Peel Technologies Inc. All Rights Reserved.
package com.peel.appscope;

@FunctionalInterface
public interface InstanceFactory<T> {
    T create();
}
