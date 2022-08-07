FROM openjdk:8u292
ARG db_name
ARG db_user
ARG db_pass
ARG key_pass
ARG cos_secret_id
ARG cos_secret_key
ARG cos_region
ARG cos_bucket
ARG redis_port
ARG rsa_private_key
ARG qp_app_id
ARG qp_app_secret
ARG salt
ARG my_jw_username
ARG my_jw_password
ARG mail_user
ARG mail_password
ENV DB_NAME=$db_name \
    DB_USER=$db_user \
    DB_PASS=$db_pass \
    KEY_PASS=$key_pass \
    COS_SECRET_ID=$cos_secret_id \
    COS_SECRET_KEY=$cos_secret_key \
    COS_REGION=$cos_region \
    COS_BUCKET=$cos_bucket \
    REDIS_PORT=$redis_port \
    RSA_PRIVATE_KEY=$rsa_private_key \
    QP_APP_ID=$qp_app_id \
    QP_APP_SECRET=$qp_app_secret \
    SALT=$salt \
    MY_JW_USERNAME=$my_jw_username \
    MY_JW_PASSWORD=$my_jw_password \
    MAIL_USER=$mail_user \
    MAIL_PASSWORD=$mail_password
ADD target/weihashi-2.0.0.jar /server/weihashi.jar
ADD devmeteor.cn.jks /server/devmeteor.cn.jks
ADD run.sh /run.sh
ENTRYPOINT ["sh","run.sh"]