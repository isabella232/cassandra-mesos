/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mesosphere.mesos.util;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import io.mesosphere.mesos.frameworks.cassandra.CassandraFrameworkProtos;
import io.mesosphere.mesos.frameworks.cassandra.CassandraFrameworkProtos.*;

import org.apache.mesos.Protos.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;

public final class CassandraFrameworkProtosUtils {

    private CassandraFrameworkProtosUtils() {}

    @NotNull
    public static Function<ExecutorMetadata, String> executorMetadataToIp() {
        return SlaveMetadataToIp.INSTANCE;
    }

    @NotNull
    public static Function<CassandraNode, String> cassandraNodeToIp() {
        return CassandraNodeToIp.INSTANCE;
    }

    @NotNull
    public static Function<CassandraFrameworkProtos.CassandraNode, CassandraFrameworkProtos.CassandraNode.Builder> cassandraNodeToBuilder() {
        return CassandraNodeToBuilder.INSTANCE;
    }

    @NotNull
    public static Function<CassandraNode.Builder, CassandraNode> cassandraNodeBuilderToCassandraNode() {
        return CassandraNodeBuilderToCassandraNode.INSTANCE;
    }

    @NotNull
    public static Function<CassandraFrameworkProtos.ExecutorMetadata, CassandraFrameworkProtos.ExecutorMetadata.Builder> executorMetadataToBuilder() {
        return ExecutorMetadataToBuilder.INSTANCE;
    }

    @NotNull
    public static Function<ExecutorMetadata.Builder, ExecutorMetadata> executorMetadataBuilderToExecutorMetadata() {
        return ExecutorMetadataBuilderToExecutorMetadata.INSTANCE;
    }

    @NotNull
    public static Predicate<CassandraNode> cassandraNodeForTaskId(@NotNull final String taskId) {
        return new CassandraNodeTaskIdEq(taskId);
    }

    @NotNull
    public static Predicate<CassandraNode> cassandraNodeHasExecutor() {
        return new CassandraNodeHasExecutor();
    }

    @NotNull
    public static Function<CassandraNode, String> executorIdFromCassandraNode() {
        return ExecutorIdFromCassandraNode.INSTANCE;
    }

    @NotNull
    public static Predicate<CassandraNode> cassandraNodeHostnameEq(@NotNull final String hostname) {
        return new CassandraNodeHostnameEq(hostname);
    }

    @NotNull
    public static Predicate<ExecutorMetadata> executorMetadataExecutorIdEq(@NotNull final String executorId) {
        return new ExecutorMetadataExecutorIdEq(executorId);
    }

    @NotNull
    public static Predicate<CassandraNode> cassandraNodeExecutorIdEq(@NotNull final String executorId) {
        return new CassandraNodeExecutorIdEq(executorId);
    }

    @NotNull
    public static Predicate<HealthCheckHistoryEntry> healthCheckHistoryEntryExecutorIdEq(@NotNull final String executorId) {
        return new HealthCheckHistoryEntryExecutorIdEq(executorId);
    }

    @NotNull
    public static Predicate<Resource> resourceHasExpectedRole(@NotNull final String role) {
        return new ResourceHasExpectedRole(role);
    }

    @NotNull
    public static Function<HealthCheckHistoryEntry, String> healthCheckHistoryEntryToExecutorId() {
        return HealthCheckHistoryEntryToExecutorId.INSTANCE;
    }

    @NotNull
    public static Function<HealthCheckHistoryEntry, Long> healthCheckHistoryEntryToTimestamp() {
        return HealthCheckHistoryEntryToTimestamp.INSTANCE;
    }

    @NotNull
    public static URI resourceUri(@NotNull final String urlForResource, final boolean extract) {
        return URI.newBuilder().setValue(urlForResource).setExtract(extract).build();
    }

    public static TaskConfig.Entry configValue(@NotNull final String name, @NotNull final Integer value) {
        return TaskConfig.Entry.newBuilder().setName(name).setLongValue(value).build();
    }

    public static TaskConfig.Entry configValue(@NotNull final String name, @NotNull final String value) {
        return TaskConfig.Entry.newBuilder().setName(name).setStringValue(value).build();
    }

    public static List<String> getSeedNodeIps(@NotNull List<CassandraNode> nodes) {
        return newArrayList(from(nodes)
            .filter(new Predicate<CassandraNode>() {
                @Override
                public boolean apply(CassandraNode v) {
                    return v.getSeed();
                }
            })
            .transform(cassandraNodeToIp()));
    }

    public static void addTaskEnvEntry(@NotNull final TaskEnv.Builder taskEnv, boolean replace, @NotNull final String name, @NotNull final String value) {
        for (int i = 0; i < taskEnv.getVariablesList().size(); i++) {
            TaskEnv.Entry entry = taskEnv.getVariables(i);
            if (name.equals(entry.getName())) {
                if (replace)
                    taskEnv.setVariables(i, TaskEnv.Entry.newBuilder().setName(name).setValue(value).build());
                return;
            }
        }
        taskEnv.addVariables(TaskEnv.Entry.newBuilder().setName(name).setValue(value).build());
    }

    public static void setTaskConfig(@NotNull final TaskConfig.Builder taskConfig, @NotNull final TaskConfig.Entry entry) {
        for (int i = 0; i < taskConfig.getVariablesList().size(); i++) {
            TaskConfig.Entry e = taskConfig.getVariables(i);
            if (entry.getName().equals(e.getName())) {
                taskConfig.setVariables(i, entry);
                return;
            }
        }
        taskConfig.addVariables(entry);
    }

    public static CassandraNodeTask getTaskForNode(@NotNull CassandraNode cassandraNode, @NotNull String taskId) {
        for (CassandraNodeTask cassandraNodeTask : cassandraNode.getTasksList()) {
            if (cassandraNodeTask.getTaskId().equals(taskId))
                return cassandraNodeTask;
        }
        return null;
    }

    public static CassandraNodeTask getTaskForNode(@NotNull CassandraNode cassandraNode, @NotNull CassandraNodeTask.TaskType taskType) {
        for (CassandraNodeTask cassandraNodeTask : cassandraNode.getTasksList()) {
            if (cassandraNodeTask.getTaskType() == taskType)
                return cassandraNodeTask;
        }
        return null;
    }

    public static CassandraNode.Builder removeTask(@NotNull CassandraNode cassandraNode, @NotNull CassandraNodeTask nodeTask) {
        CassandraNode.Builder builder = CassandraNode.newBuilder(cassandraNode);
        for (int i = 0; i < builder.getTasksList().size(); i++) {
            CassandraNodeTask t = builder.getTasks(i);
            if (t.getTaskId().equals(nodeTask.getTaskId())) {
                builder.removeTasks(i);
                break;
            }
        }
        return builder;
    }

    private static final class SlaveMetadataToIp implements Function<ExecutorMetadata, String> {
        private static final SlaveMetadataToIp INSTANCE = new SlaveMetadataToIp();

        @Override
        public String apply(@NotNull final ExecutorMetadata input) {
            return input.getIp();
        }
    }

    private static final class CassandraNodeToIp implements Function<CassandraNode, String> {
        private static final CassandraNodeToIp INSTANCE = new CassandraNodeToIp();

        @Override
        public String apply(@NotNull final CassandraNode input) {
            return input.getIp();
        }
    }

    private static final class CassandraNodeToBuilder implements Function<CassandraFrameworkProtos.CassandraNode, CassandraFrameworkProtos.CassandraNode.Builder> {
        private static final CassandraNodeToBuilder INSTANCE = new CassandraNodeToBuilder();

        @Override
        public CassandraNode.Builder apply(@NotNull final CassandraNode input) {
            return CassandraNode.newBuilder(input);
        }
    }

    private static final class CassandraNodeBuilderToCassandraNode implements Function<CassandraNode.Builder, CassandraNode> {
        private static final CassandraNodeBuilderToCassandraNode INSTANCE = new CassandraNodeBuilderToCassandraNode();

        @Override
        public CassandraNode apply(@NotNull final CassandraNode.Builder input) {
            return input.build();
        }
    }

    private static final class ExecutorMetadataToBuilder implements Function<CassandraFrameworkProtos.ExecutorMetadata, CassandraFrameworkProtos.ExecutorMetadata.Builder> {
        private static final ExecutorMetadataToBuilder INSTANCE = new ExecutorMetadataToBuilder();

        @Override
        public ExecutorMetadata.Builder apply(@NotNull final ExecutorMetadata input) {
            return ExecutorMetadata.newBuilder(input);
        }
    }

    private static final class ExecutorMetadataBuilderToExecutorMetadata implements Function<ExecutorMetadata.Builder, ExecutorMetadata> {
        private static final ExecutorMetadataBuilderToExecutorMetadata INSTANCE = new ExecutorMetadataBuilderToExecutorMetadata();

        @Override
        public ExecutorMetadata apply(@NotNull final ExecutorMetadata.Builder input) {
            return input.build();
        }
    }

    private static final class CassandraNodeTaskIdEq implements Predicate<CassandraNode> {
        @NotNull
        private final String taskId;

        public CassandraNodeTaskIdEq(@NotNull final String taskId) {
            this.taskId = taskId;
        }

        @Override
        public boolean apply(@NotNull final CassandraNode item) {
            for (CassandraNodeTask cassandraNodeTask : item.getTasksList()) {
                if (cassandraNodeTask.getTaskId().equals(taskId))
                    return true;
            }
            return false;
        }
    }

    private static final class ExecutorIdFromCassandraNode implements Function<CassandraNode, String> {
        private static final ExecutorIdFromCassandraNode INSTANCE = new ExecutorIdFromCassandraNode();

        @Override
        public String apply(@NotNull final CassandraNode input) {
            return input.getCassandraNodeExecutor().getExecutorId();
        }
    }

    private static final class CassandraNodeHasExecutor implements Predicate<CassandraNode> {
        @Override
        public boolean apply(@NotNull final CassandraNode item) {
            return item.hasCassandraNodeExecutor();
        }
    }

    private static final class CassandraNodeHostnameEq implements Predicate<CassandraNode> {
        @NotNull
        private final String hostname;

        public CassandraNodeHostnameEq(@NotNull final String hostname) {
            this.hostname = hostname;
        }

        @Override
        public boolean apply(@NotNull final CassandraNode item) {
            return item.hasHostname() && item.getHostname().equals(hostname);
        }
    }

    private static final class ExecutorMetadataExecutorIdEq implements Predicate<ExecutorMetadata> {
        @NotNull
        private final String executorId;

        public ExecutorMetadataExecutorIdEq(@NotNull final String executorId) {
            this.executorId = executorId;
        }

        @Override
        public boolean apply(@NotNull final ExecutorMetadata item) {
            return item.getExecutorId().equals(executorId);
        }
    }

    private static final class CassandraNodeExecutorIdEq implements Predicate<CassandraNode> {
        @NotNull
        private final String executorId;

        public CassandraNodeExecutorIdEq(@NotNull final String executorId) {
            this.executorId = executorId;
        }

        @Override
        public boolean apply(@NotNull final CassandraNode item) {
            return item.hasCassandraNodeExecutor() && item.getCassandraNodeExecutor().getExecutorId().equals(executorId);
        }
    }

    private static final class HealthCheckHistoryEntryExecutorIdEq implements Predicate<HealthCheckHistoryEntry> {
        @NotNull
        private final String executorId;

        public HealthCheckHistoryEntryExecutorIdEq(@NotNull final String executorId) {
            this.executorId = executorId;
        }

        @Override
        public boolean apply(@NotNull final HealthCheckHistoryEntry item) {
            return item.getExecutorId().equals(executorId);
        }
    }

    private static final class ResourceHasExpectedRole implements Predicate<Resource> {
        @NotNull
        private final String role;

        public ResourceHasExpectedRole(@NotNull final String role) {
            this.role = role;
        }

        @Override
        public boolean apply(final Resource item) {
            return item.getRole().equals(role);
        }
    }

    private static final class HealthCheckHistoryEntryToExecutorId implements Function<HealthCheckHistoryEntry, String> {
        private static final HealthCheckHistoryEntryToExecutorId INSTANCE = new HealthCheckHistoryEntryToExecutorId();

        @Override
        public String apply(@NotNull final HealthCheckHistoryEntry input) {
            return input.getExecutorId();
        }
    }

    private static final class HealthCheckHistoryEntryToTimestamp implements Function<HealthCheckHistoryEntry, Long> {
        private static final HealthCheckHistoryEntryToTimestamp INSTANCE = new HealthCheckHistoryEntryToTimestamp();

        @Override
        public Long apply(@NotNull final HealthCheckHistoryEntry input) {
            return input.getTimestampEnd();
        }
    }

    private static final class CassandraNodeToExecutor implements Function<CassandraNode, CassandraNodeExecutor> {
        private static final CassandraNodeToExecutor INSTANCE = new CassandraNodeToExecutor();

        @Override
        public CassandraNodeExecutor apply(@NotNull final CassandraNode input) {
            return input.getCassandraNodeExecutor();
        }
    }

    @NotNull
    public static Function<CassandraNode, CassandraNodeExecutor> cassandraNodeToExecutor() {
        return CassandraNodeToExecutor.INSTANCE;
    }


}