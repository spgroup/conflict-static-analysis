FROM ubuntu:22.04

# -------------------------------------
# 1. Instalar dependências de sistema
# -------------------------------------
RUN apt-get update --fix-missing && \
    apt-get install -y openjdk-8-jdk ant curl python3 python3-pip git vim ca-certificates-java && \
    apt-get clean && \
    update-ca-certificates -f

# -------------------------------------
# 2. Configurar JAVA_HOME
# -------------------------------------
ENV JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/
ENV PATH=$JAVA_HOME/bin:$PATH

# -------------------------------------
# 3. Instalar Maven
# -------------------------------------
ARG MAVEN_VERSION=3.8.9
ARG USER_HOME_DIR="/root"
ARG BASE_URL=https://dlcdn.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries

RUN mkdir -p /usr/share/maven /usr/share/maven/ref && \
    curl -fsSL -o /tmp/apache-maven.tar.gz ${BASE_URL}/apache-maven-${MAVEN_VERSION}-bin.tar.gz && \
    tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 && \
    rm -f /tmp/apache-maven.tar.gz && \
    ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"

# -------------------------------------
# 4. Prepare miningframework
# -------------------------------------

RUN git clone --recursive --branch article_ioa_2025 --depth 1 https://github.com/spgroup/miningframework.git /home/tmp/miningframework
RUN pip install -r /home/tmp/miningframework/requirements.txt

RUN mkdir -p /home/mds /home/rds && \
    cp -r /home/tmp/miningframework /home/mds/miningframework && \
    cp -r /home/tmp/miningframework /home/rds/miningframework && \
    rm -rf /home/tmp

RUN mkdir -p /home/mds/miningframework/output/data
ADD results-with-build-information.csv /home/mds/miningframework/output/data/


# -------------------------------------
# 5. Prepare conflict-static-analysis
# -------------------------------------
ADD ioa-experiment/settings.xml $USER_HOME_DIR/.m2/
RUN git clone --branch article_ioa_2025 --depth 1 https://github.com/spgroup/conflict-static-analysis.git /home/conflict-static-analysis

RUN cd /home/conflict-static-analysis && mvn clean install -DskipTests && mvn compile assembly:single && \
    cp /home/conflict-static-analysis/target/*.jar /home/mds/miningframework/dependencies/ && \
    cp /home/conflict-static-analysis/target/*.jar /home/rds/miningframework/dependencies/


# -------------------------------------
# 6. Preparar datasets
# -------------------------------------

RUN git clone --branch article_ioa_2025 --depth 1 https://github.com/spgroup/mergedataset.git /home/mds/miningframework/output/files
#RUN /home/mds/miningframework/output/files/prepareDataset.sh

RUN git clone --branch article_ioa_2025 --depth 1 https://github.com/barbosamaatheus/ref-dataset.git /home/rds/miningframework/output/files
RUN /home/rds/miningframework/output/files/prepare-dataset.sh

# -------------------------------------
# 7. Copiar scripts para o container
# -------------------------------------
COPY scripts/run_and_organize_ioa_experimental_results.py /home/mds/miningframework/run_and_organize_ioa_experimental_results.py
COPY scripts/run_and_organize_ioa_experimental_results.py /home/rds/miningframework/run_and_organize_ioa_experimental_results.py

COPY scripts /home/mds/miningframework/results
COPY scripts /home/rds/miningframework/results

COPY entrypoint.sh /home/
RUN chmod +x /home/entrypoint.sh && chmod +x /home/mds/miningframework/gradlew && chmod +x /home/rds/miningframework/gradlew

# -------------------------------------
# 8. Instalar dependências dos scripts experimentais
# -------------------------------------
RUN pip install -r /home/mds/miningframework/results/requirements.txt
RUN pip install -r /home/rds/miningframework/results/requirements.txt
# -------------------------------------
# 11. Definir comando de execução do container
# -------------------------------------
CMD ["/home/run_experiment.sh"]
