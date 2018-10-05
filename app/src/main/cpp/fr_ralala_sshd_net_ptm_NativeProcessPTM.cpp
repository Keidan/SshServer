/**
 * ******************************************************************************
 * <p><b>Project SshServer</b><br/>
 * JNI implementation for the NativeProcessPTM class.
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
/* Includes ------------------------------------------------------------------*/
#include "fr_ralala_sshd_net_ptm_NativeProcessPTM.h"
#include <string>
#include <vector>
#include <errno.h>
#include <sys/ioctl.h>
#include <sys/wait.h>
#include <termios.h>
#include <unistd.h>
#include <cstdlib>
#include <fcntl.h>

/* Defines -------------------------------------------------------------------*/
#define FIELD_PID_NP "mPid"
#define SIGNATURE_PID_NP "I"
#define FIELD_DESCRIPTOR_NP "mDescriptor"
#define SIGNATURE_DESCRIPTOR_NP "I"

/* Global variables ----------------------------------------------------------*/

/* Static functions -----------------------------------------------------------*/
/**
 * @brief Converts a java string to c++ string.
 * @param env JNI environment.
 * @param jstr java string.
 * @return c++ string
 */
static auto getStringUTF(JNIEnv *env, jstring jstr) -> std::string {
  jboolean isCopy;
  auto utf_string = env->GetStringUTFChars(jstr, &isCopy);
  std::string str = utf_string;
  if (isCopy == JNI_TRUE)
    env->ReleaseStringUTFChars(jstr, utf_string);
  return str;
}

/**
 * @brief Creates a forked process using the pseudo-terminal ptm.
 * @param jenv JNI environment
 * @param pid The output PID.
 * @param cmd The command to execute.
 * @param args The command arguments.
 * @return The FD of the ptm device.
 */
static auto ptmFork(JNIEnv *jenv, pid_t* pid, std::string cmd, std::vector<char const *> args) -> int {
  char* devname;
  int ptDevFd;

  /*
   * ptm It is a character file. It is used to create a pseudoterminal master and slave pair.
   * same as open("/dev/ptmx", O_RDWR | O_NOCTTY);
   */
  ptDevFd = getpt();
  if(ptDevFd < 0){
    auto err = "Unable to open ptm file: " + std::string(strerror(errno));
    auto ex = jenv->FindClass("java/io/IOException");
    jenv->ThrowNew(ex, err.c_str());
    return -1;
  }
  /* The file descriptor should be closed when an exec function is invoked. */
  fcntl(ptDevFd, F_SETFD, FD_CLOEXEC);

  /*
   * - grantpt: Changes the mode and owner of the slave pseudoterminal device
   * corresponding to the master pseudoterminal referred to by fd.
   * - unlockpt: Unlocks the slave pseudoterminal device
   * corresponding to the master pseudoterminal referred to by fd.
   * -ptsname: Returns the name of the slave pseudoterminal device
   * corresponding to the master referred to by fd.
   */
  if(grantpt(ptDevFd) || unlockpt(ptDevFd) || ((devname = ptsname(ptDevFd)) == 0)){
    auto err = "Unable to grant or unlock pt: " + std::string(strerror(errno));
    auto ex = jenv->FindClass("java/io/IOException");
    jenv->ThrowNew(ex, err.c_str());
    return -1;
  }
  /* Creates a new process. */
  *pid = fork();
  if(*pid < 0) {
    auto err = "Unable to create fork: " + std::string(strerror(errno));
    auto ex = jenv->FindClass("java/io/IOException");
    jenv->ThrowNew(ex, err.c_str());
    return -1;
  }
  /* Success. */
  if(*pid == 0){
    int ptOpenFd;
    /* The function below shall create a new session, if the calling process is not a process group leader. */
    setsid();
    /* Opens the ptm device using its name. */
    ptOpenFd = open(devname, O_RDWR);
    if(ptOpenFd < 0) exit(-1);

    /* Duplicate ptm to stdin */
    dup2(ptOpenFd, 0);
    /* Duplicate ptm to stdout */
    dup2(ptOpenFd, 1);
    /* Duplicate ptm to stderr */
    dup2(ptOpenFd, 2);
    /* Close the unused FD. */
    close(ptDevFd);
    /* Exec the command. */
    execvp(cmd.c_str(), (char * const *)args.data());
    exit(-1);
  } else {
    return ptDevFd;
  }
}



/* Public functions ----------------------------------------------------------*/
/*
 * Class:     fr_ralala_sshd_net_ptm_NativeProcessPTM
 * Method:    create0
 * Signature: (Ljava/lang/String;[Ljava/lang/String;[Ljava/lang/String;)Lfr/ralala/sshd/net/ptm/NativeProcessPTM;
 */
JNIEXPORT auto JNICALL Java_fr_ralala_sshd_net_ptm_NativeProcessPTM_create0
    (JNIEnv *env, jclass clazz, jstring jcmd, jobjectArray jargs, jobjectArray jarrayEnv) -> jobject {

  /* JAVA ID's */
  /* Calculates the constructor's ID in order to instantiate it. */
  auto nativeProcessConstructor = env->GetMethodID(clazz, "<init>", "()V");
  if (nativeProcessConstructor == nullptr) {
    auto ex = env->FindClass("java/lang/NoSuchMethodException");
    env->ThrowNew(ex, "NativeProcess: Default constructor not found");
    return nullptr;
  }
  /* Retrieves the pid field that is declared on the java side. */
  auto nativeProcessFieldPid = env->GetFieldID(clazz, FIELD_PID_NP, SIGNATURE_PID_NP);
  if (nativeProcessFieldPid == nullptr) {
    auto ex = env->FindClass("java/lang/NoSuchFieldException");
    env->ThrowNew(ex, "NativeProcess: Field '" FIELD_PID_NP "' not found");
    return nullptr;
  }
  /* Retrieves the descriptor field that is declared on the java side. */
  auto nativeProcessFieldDescriptor = env->GetFieldID(clazz, FIELD_DESCRIPTOR_NP, SIGNATURE_DESCRIPTOR_NP);
  if (nativeProcessFieldDescriptor == nullptr) {
    auto ex = env->FindClass("java/lang/NoSuchFieldException");
    env->ThrowNew(ex, "NativeProcess: Field '" FIELD_DESCRIPTOR_NP "' not found");
    return nullptr;
  }

  /* Parses arguments */
  int columns = 0, lines = 0;
  /* Prepare to execute the command. */
  auto cmd = getStringUTF(env, jcmd);
  /* Parses the arguments */
  std::vector<char const *> args;
  if(jargs != nullptr) {
    auto count = env->GetArrayLength(jargs);
    if(count != 0) {
      args.reserve(((std::size_t)count)+2);
      args.push_back(cmd.c_str());
      for (int i = 0; i < count; i++) {
        jstring str = (jstring)env->GetObjectArrayElement(jargs, i);
        args.push_back(getStringUTF(env, str).c_str());
      }
    } else {
      args.reserve(2); /* For args[0] + null terminated. */
      args.push_back(cmd.c_str()); /* Force args[0] */
    }
  } else {
    args.reserve(2); /* For args[0] + null terminated. */
    args.push_back(cmd.c_str()); /* Force args[0] */
  }
  args.push_back(nullptr); /* null terminated */
  /* Parses and sets the env variables. */
  if(jarrayEnv != nullptr) {
    auto count = env->GetArrayLength(jarrayEnv);
    if(count != 0) {
      for (int i = 0; i < count; i++) {
        auto jstr =  (jstring)env->GetObjectArrayElement(jarrayEnv, i);
        auto str = getStringUTF(env, jstr);
        auto delim = str.find("=");
        if (delim != std::string::npos) {
          auto key = str.substr(0, delim);
          auto value = str.substr(delim + 1);
          if(key.compare("COLUMNS") == 0)
            columns = std::stoi(value);
          else if(key.compare("LINES") == 0)
            lines = std::stoi(value);
          setenv(key.c_str(), value.c_str(), 1);
        }
      }
    }
  }
  /* We try to create an instance of the java object based on the class identifiers and the constructor identifier. */
  auto instanceNativeProcess = env->NewObject(clazz, nativeProcessConstructor);
  if (instanceNativeProcess == nullptr) {
    auto ex = env->FindClass("java/io/IOException");
    env->ThrowNew(ex, "NativeProcess: Unable to create a new object");
    return nullptr;
  }
  pid_t pid;
  auto ptFd = ptmFork(env, &pid, cmd, args);
  env->SetIntField(instanceNativeProcess, nativeProcessFieldPid, pid);
  env->SetIntField(instanceNativeProcess, nativeProcessFieldDescriptor, ptFd);

  /* updates pseudo terminal size */
  if(ptFd > 0 && lines > 0 && columns > 0) {
    struct winsize sz;
    sz.ws_row = (unsigned short) lines;
    sz.ws_col = (unsigned short) columns;
    sz.ws_xpixel = sz.ws_ypixel = 0; /* unused */
    ioctl(ptFd, TIOCSWINSZ, &sz);
  }
  return instanceNativeProcess;
}

/*
 * Class:     fr_ralala_sshd_net_ptm_NativeProcessPTM
 * Method:    waitFor
 * Signature: ()I
 */
JNIEXPORT auto JNICALL Java_fr_ralala_sshd_net_ptm_NativeProcessPTM_waitFor
    (JNIEnv *env, jobject instance) -> jint {
  auto nativeProcessClass = env->GetObjectClass(instance);
  if (nativeProcessClass == nullptr) {
    auto ex = env->FindClass("java/lang/NoSuchClassException");
    env->ThrowNew(ex, "Class 'NativeProcess' not found");
    return -1;
  }
  /* Retrieves the pid field that is declared on the java side. */
  auto nativeProcessFieldPid = env->GetFieldID(nativeProcessClass, FIELD_PID_NP, SIGNATURE_PID_NP);
  if (nativeProcessFieldPid == nullptr) {
    auto ex = env->FindClass("java/lang/NoSuchFieldException");
    env->ThrowNew(ex, "NativeProcess: Field '" FIELD_PID_NP "' not found");
    return -1;
  }

  /* Gets the field value. */
  auto pid = env->GetIntField(instance, nativeProcessFieldPid);
  /* Calls to waitpid */
  int status;
  waitpid(pid, &status, 0);
  return (WIFEXITED(status)) ? WEXITSTATUS(status) : 0;
}