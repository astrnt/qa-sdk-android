package co.astrnt.qasdk.repository;

import java.util.HashMap;

import co.astrnt.qasdk.core.AstronautApi;
import co.astrnt.qasdk.core.MyObserver;
import co.astrnt.qasdk.dao.BaseApiDao;
import co.astrnt.qasdk.dao.InterviewApiDao;
import co.astrnt.qasdk.dao.LogDao;
import co.astrnt.qasdk.dao.MultipleAnswerApiDao;
import co.astrnt.qasdk.dao.QuestionApiDao;
import co.astrnt.qasdk.dao.SectionApiDao;
import co.astrnt.qasdk.type.ElapsedTime;
import co.astrnt.qasdk.type.ElapsedTimeType;
import co.astrnt.qasdk.type.InterviewType;
import co.astrnt.qasdk.type.TestType;
import co.astrnt.qasdk.utils.LogUtil;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.RealmList;
import timber.log.Timber;

/**
 * Created by deni rohimat on 27/04/18.
 */
public class QuestionRepository extends BaseRepository {

    public QuestionRepository(AstronautApi astronautApi) {
        super(astronautApi);
    }

    public Observable<BaseApiDao> addQuestionAttempt(QuestionApiDao currentQuestion) {
        InterviewApiDao interviewApiDao = astrntSDK.getCurrentInterview();
        String token = interviewApiDao.getToken();

        HashMap<String, String> map = new HashMap<>();
        map.put("interview_code", interviewApiDao.getInterviewCode());
        map.put("candidate_id", String.valueOf(interviewApiDao.getCandidate().getId()));
        map.put("question_id", String.valueOf(currentQuestion.getId()));

        if (astrntSDK.isSectionInterview()) {
            SectionApiDao currentSection = astrntSDK.getCurrentSection();
            map.put("section_id", String.valueOf(currentSection.getId()));
        }

        LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                new LogDao("Hit API (/question/attempt)",
                        "Add Question Attempt, number " + (astrntSDK.getQuestionIndex() + 1) +
                                ", questionId = " + currentQuestion.getId()
                )
        );
        astrntSDK.saveLastApiCall("(/question/attempt)");

        return mAstronautApi.getApiService().addAttempt(token, map);
    }

    public Observable<BaseApiDao> addMediaAttempt(QuestionApiDao currentQuestion) {
        InterviewApiDao interviewApiDao = astrntSDK.getCurrentInterview();
        String token = interviewApiDao.getToken();

        HashMap<String, String> map = new HashMap<>();
        map.put("interview_code", interviewApiDao.getInterviewCode());
        map.put("candidate_id", String.valueOf(interviewApiDao.getCandidate().getId()));
        map.put("question_id", String.valueOf(currentQuestion.getId()));

        if (astrntSDK.isSectionInterview()) {
            SectionApiDao currentSection = astrntSDK.getCurrentSection();
            map.put("section_id", String.valueOf(currentSection.getId()));
        }

        LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                new LogDao("Hit API (/media/attempt)",
                        "Add Media Attempt, number " + (astrntSDK.getQuestionIndex() + 1) +
                                ", questionId = " + currentQuestion.getId()
                )
        );
        astrntSDK.saveLastApiCall("(/media/attempt)");

        return mAstronautApi.getApiService().addMediaAttempt(token, map);
    }

    public Observable<BaseApiDao> finishQuestion(QuestionApiDao currentQuestion) {
        InterviewApiDao interviewApiDao = astrntSDK.getCurrentInterview();
        String token = interviewApiDao.getToken();

        HashMap<String, String> map = new HashMap<>();
        map.put("interview_code", interviewApiDao.getInterviewCode());
        map.put("candidate_id", String.valueOf(interviewApiDao.getCandidate().getId()));
        map.put("question_id", String.valueOf(currentQuestion.getId()));

        if (astrntSDK.isSectionInterview()) {
            SectionApiDao currentSection = astrntSDK.getCurrentSection();
            map.put("section_id", String.valueOf(currentSection.getId()));
        }

        if (interviewApiDao.getType().equals(InterviewType.CLOSE_TEST)) {
            updateElapsedTime(ElapsedTimeType.TEST, currentQuestion.getId());
        }

        LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                new LogDao("Hit API (/question/finish)",
                        "Finish Question, number " + (astrntSDK.getQuestionIndex() + 1) +
                                ", questionId = " + currentQuestion.getId()
                )
        );
        astrntSDK.saveLastApiCall("(/question/finish)");

        return mAstronautApi.getApiService().finishQuestion(token, map);
    }

    public Observable<BaseApiDao> answerQuestion(QuestionApiDao currentQuestion) {
        InterviewApiDao interviewApiDao = astrntSDK.getCurrentInterview();
        String token = interviewApiDao.getToken();

        HashMap<String, String> map = new HashMap<>();
        map.put("interview_code", interviewApiDao.getInterviewCode());
        map.put("candidate_id", String.valueOf(interviewApiDao.getCandidate().getId()));
        map.put("question_id", String.valueOf(currentQuestion.getId()));
        map.put("invite_id", String.valueOf(interviewApiDao.getInvite_id()));

        if (currentQuestion.getType_child().equals(TestType.FREE_TEXT)) {
            map.put("type", "1");
            String currentAnswer = currentQuestion.getAnswer();
            if (currentAnswer == null) {
                currentAnswer = "";
            }
            map.put("text_answer", currentAnswer);

        } else {
            map.put("type", "0");

            RealmList<MultipleAnswerApiDao> selectedAnswer = currentQuestion.getSelectedAnswer();

            if (selectedAnswer != null) {
                for (int i = 0; i < selectedAnswer.size(); i++) {
                    MultipleAnswerApiDao answerItem = selectedAnswer.get(i);

                    assert answerItem != null;
                    map.put("answer_ids[" + i + "]", String.valueOf(answerItem.getId()));
                }
            }

        }

        if (astrntSDK.isSectionInterview()) {
            map.put("interview_type", "section");
        } else {
            map.put("interview_type", "test");
        }

        if (astrntSDK.isSectionInterview()) {
            LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                    new LogDao("Hit API (/question/answer)",
                            "Answer Question " + (astrntSDK.getQuestionIndex() + 1) +
                                    ", questionId = " + currentQuestion.getId() +
                                    ", sectionId = " + astrntSDK.getCurrentSection().getId() +
                                    " duration left = " + astrntSDK.getCurrentSection().getDuration() +
                                    " seconds"
                    )
            );
        } else {
            LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                    new LogDao("Hit API (/question/answer)",
                            "Answer Question " + (astrntSDK.getQuestionIndex() + 1) +
                                    ", questionId = " + currentQuestion.getId()
                    )
            );
        }
        astrntSDK.saveLastApiCall("(/question/answer)");

        return mAstronautApi.getApiService().answerQuestion(token, map);
    }

    private void updateElapsedTime(@ElapsedTime String type, long refId) {
        InterviewApiDao interviewApiDao = astrntSDK.getCurrentInterview();

        HashMap<String, String> map = new HashMap<>();
        map.put("interview_code", interviewApiDao.getInterviewCode());
        map.put("type", type);
        map.put("ref_id", String.valueOf(refId));

        String token = interviewApiDao.getToken();

        final String interviewCode = astrntSDK.getInterviewCode();
        LogUtil.addNewLog(interviewCode,
                new LogDao("Hit API (/interview/update/elapsedTime)",
                        "Update Elapsed Time, type =  " + type +
                                ", number " + (astrntSDK.getQuestionIndex() + 1)
                                + ", refId = " + refId
                )
        );

        astrntSDK.saveLastApiCall("(/interview/update/elapsedTime)");

        mAstronautApi.getApiService().updateElapsedTime(token, map)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new MyObserver<BaseApiDao>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onApiResultCompleted() {
                    }

                    @Override
                    public void onApiResultError(String title, String message, String code) {
                        Timber.e(message);

                        LogUtil.addNewLog(interviewCode,
                                new LogDao("Hit API (Elapsed Time)",
                                        "Error " + message
                                )
                        );

                        astrntSDK.saveLastApiCall("(Elapsed Time)");
                    }

                    @Override
                    public void onApiResultOk(BaseApiDao apiDao) {
                        Timber.d(apiDao.getMessage());
                    }
                });
    }

    public Observable<BaseApiDao> addLastSeen(QuestionApiDao currentQuestion) {
        InterviewApiDao interviewApiDao = astrntSDK.getCurrentInterview();
        String token = interviewApiDao.getToken();

        HashMap<String, String> map = new HashMap<>();
        map.put("interview_code", interviewApiDao.getInterviewCode());
        map.put("question_id", String.valueOf(currentQuestion.getId()));

        LogUtil.addNewLog(interviewApiDao.getInterviewCode(),
                new LogDao("Hit API (/question/last_seen)",
                        "Add Last Seen, number " + (astrntSDK.getQuestionIndex() + 1) +
                                ", questionId = " + currentQuestion.getId()
                )
        );
        astrntSDK.saveLastApiCall("(/question/last_seen)");

        return mAstronautApi.getApiService().addLastSeen(token, map);
    }


}
