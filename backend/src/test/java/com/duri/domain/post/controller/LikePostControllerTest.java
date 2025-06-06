package com.duri.domain.post.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.duri.domain.auth.CustomUserDetails;
import com.duri.domain.post.dto.PostLikeStatusResponseDto;
import com.duri.domain.post.service.LikePostService;
import com.duri.domain.user.entity.User;
import com.duri.global.util.AESUtilTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LikePostController.class)
@DisplayName("게시글 좋아요 Controller")
@AutoConfigureRestDocs(outputDir = "build/generated-snippets")
@AutoConfigureMockMvc()
class LikePostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LikePostService likePostService;

    private Authentication getTestAuthentication() {
        User user = User.builder()
            .id(1L)
            .coupleCode("COUPLE123")
            .build();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        return new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(),
            userDetails.getAuthorities());
    }

    @Test
    @DisplayName("POST /post/like")
    void likePost() throws Exception {
        // given
        Long postId = 1L;

        // when & then
        mockMvc.perform(post("/post/like", postId)
                .with(authentication(getTestAuthentication()))
                .with(csrf()))
            .andExpect(status().isOk())
            .andDo(document("post-like",
                requestFields(
                    fieldWithPath("postIdToken").description("게시글 ID 토큰")
                )
            ));
    }

    @Test
    @DisplayName("POST /post/dislike")
    void dislikePost() throws Exception {
        // given
        Long postId = 1L;

        // when & then
        mockMvc.perform(post("/post/dislike", postId)
                .with(authentication(getTestAuthentication()))
                .with(csrf()))
            .andExpect(status().isOk())
            .andDo(document("post-dislike",
                requestFields(
                    fieldWithPath("postIdToken").description("게시글 ID 토큰")
                )
            ));
    }

    @Test
    @DisplayName("GET /post/like/status")
    void getLikeStatus() throws Exception {
        // given
        String postIdToken = "/oThmJebEw7iBcnL43Hl2A==";
        BDDMockito.given(likePostService.getLikeStatus(anyString(), anyLong()))
            .willReturn(new PostLikeStatusResponseDto(true));

        // when & then
        mockMvc.perform(get("/post/like/status")
                .with(authentication(getTestAuthentication()))
                .param("postIdToken", postIdToken)
            )
            .andExpect(status().isOk())
            .andDo(document("post-like-status",
                requestFields(
                    fieldWithPath("postIdToken").description("게시글 ID 토큰")
                ),
                responseFields(
                    fieldWithPath("liked").description("해당 게시글을 좋아요 눌렀는지 여부")
                )
            ));
    }

    @BeforeEach
    void setUp() {
        AESUtilTestHelper.setSecretKey("1234567890abcdef");
    }
}