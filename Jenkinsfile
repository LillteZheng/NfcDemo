//这个是 Jenkins 的脚本,脚本化流水线语法的关键部分.
node(){
    //编译命令
    BUILD_COMMAND = "./gradlew clean assembleRelease"

    //配置apk生成路径
    env.APK_PATH = "${env.WORKSPACE}/app/build/outputs/apk"
    //未签名apk路径，jenkins会取未签名的apk进行签名，然后存档
    env.APK_TO_SIGN = "/app/build/outputs/*-unsigned.apk"

    try{
        //stage 是任务块
        //检查源代码
        stage('Checkout code'){
            /**
            * 必须有，该checkout步骤将检出从源控制代码;
            * scm是一个特殊变量，指示checkout步骤克隆触发此Pipeline运行的特定修订
            */
            checkout scm
        }
        //编译,sh 相当于 make
        stage('Build'){
            sh "${BUILD_COMMAND}"
        }

        currentBuild.result = 'SUCCESS'

    }catch(err){
        currentBuild.result = 'FAIL'
        throw err
    }

}

