from airflow import DAG
from airflow.operators.bash import BashOperator
from datetime import datetime

default_args = {"owner": "airflow", "start_date": datetime(2025, 1, 1)}

with DAG(
    "fraud_pipeline_dag",
    default_args=default_args,
    schedule_interval="@hourly",
    catchup=False,
) as dag:

    start_kafka = BashOperator(
        task_id="start_kafka",
        bash_command="cd /path/to/docker && docker-compose up -d"
    )

    run_streaming_job = BashOperator(
        task_id="run_spark_fraud_detection",
        bash_command="sbt run"
    )

    stop_kafka = BashOperator(
        task_id="stop_kafka",
        bash_command="cd /path/to/docker && docker-compose down"
    )

    start_kafka >> run_streaming_job >> stop_kafka
